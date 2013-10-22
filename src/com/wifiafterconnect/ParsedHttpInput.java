/*
 * Copyright (C) 2013 Sasha Vasko <sasha at aftercode dot net> 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wifiafterconnect;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.util.Log;

import com.wifiafterconnect.handlers.CaptivePageHandler;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlInput;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.Worker;

public class ParsedHttpInput extends Worker{

	private CaptivePageHandler captiveHandler = null;
	private HtmlPage htmlPage;

	public ParsedHttpInput (Worker other, URL url, String html) {
		super (other);
		parse (url, html);
	}

	public void parse(URL url, String html) {
		htmlPage = new HtmlPage(url);
		if (!html.isEmpty()) {
			if (!htmlPage.parse (html)) {
				error("Failed to parse the page - no document found");
				return;
			}

			if (!submitOnLoad()) {
				// Probably the actual login page
				captiveHandler = CaptivePageHandler.autodetect (htmlPage);
				if (captiveHandler != null)
					debug("Detected Captive portal "+ captiveHandler);
			}
		}else {
			captiveHandler = null;
		}
	}

	public String buildPostData (final WifiAuthParams authParams) {
		if (captiveHandler != null)
			return captiveHandler.getPostData(authParams);
		
		HtmlForm form = htmlPage.getForm();
		if (form != null)
			return form.formatPostData();
		
		return "";
	}
	
	public boolean checkParamsMissing(WifiAuthParams params) {
		Log.d(Constants.TAG, "checkParamsMissing: CaptiveHandler = " + captiveHandler);
		if (captiveHandler != null)
			return captiveHandler.checkParamsMissing(params);
		return false;
	}

	public WifiAuthParams addMissingParams (WifiAuthParams params) {
		Log.d(Constants.TAG, "addMissingParams: CaptiveHandler = " + captiveHandler);
		if (captiveHandler != null)
			return captiveHandler.addMissingParams(params);
		return null;
	}

	public ParsedHttpInput handleAutoRedirects (int maxRequests) {
		ParsedHttpInput result = this;

		while (result != null && --maxRequests >= 0) {
			if (result.submitOnLoad() || result.hasSubmittableForm()) {
				debug("Handling onLoad submit ...");
				result = result.postForm (null);
			}else if (result.hasMetaRefresh()) {
				debug("Handling meta refresh ...");
				result = result.getRefresh ();
			}else
				break;
		}
		return result;
	}
	
	public boolean authenticateCaptivePortal(WifiAuthParams authParams) {
		if (captiveHandler != null)
			return (captiveHandler.authenticate (this, authParams) != null);
		return false;
	}
	
	public boolean submitOnLoad() {
		String onLoad = htmlPage.getOnLoad();
		debug("OnLoad = [" + onLoad + "]");
		return onLoad.equalsIgnoreCase("document.forms[0].submit();")
				|| onLoad.equalsIgnoreCase("document.form.submit();")
				|| onLoad.endsWith(".submit();"); // this one is probably enough
	}

	public final String getHtml() {
		return htmlPage.getSource();
	}

	public final URL getUrl() {
		return htmlPage.getUrl();
	}

	public boolean hasMetaRefresh() {
		return htmlPage.hasMetaRefresh();
	}

	public URL getMetaRefreshURL() throws MalformedURLException {
		return htmlPage.getMetaRefreshURL();
	}
	
	public boolean isKnownCaptivePortal() { 
		return (captiveHandler != null);
	}

	public URL getFormPostURL() {
		HtmlForm form = htmlPage.getForm();
		if (form != null)
			return form.formatActionURL(htmlPage.getUrl());
		return htmlPage.getUrl();
	}

	public String getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasSubmittableForm() {
		return htmlPage.hasFormWithInputType(HtmlInput.TYPE_SUBMIT);
	}

	public boolean hasForm() {
		return (htmlPage.getForm() != null);
	}
	
	public String getUrlQueryVar(String varName) {
		return htmlPage.getUrlQueryVar(varName);
	}

	public ParsedHttpInput postForm (WifiAuthParams authParams) {

		URL url = getFormPostURL();
		String postDataString = buildPostData(authParams);
		String cookies = getCookies();
		
		return post (this, url, postDataString, cookies);
	}
	
	public ParsedHttpInput getRefresh () {
		ParsedHttpInput result = null;
		try {
			result = get (this, getMetaRefreshURL());
		} catch (MalformedURLException e) {
			exception (e);
		}
		
		return result;
	}

	public static ParsedHttpInput post (Worker context, URL url, String postDataString, String cookies) {
		context.debug("POST to url [" + url + "], data to post:["+postDataString+"]");
		if (postDataString == null || postDataString.isEmpty()){
			context.error("Failed to compile data for authentication POST");
			return null;
		} else if (url == null) {
			context.error("Missing URL to POST the form");
			return null;
		}

		HttpURLConnection conn = null;
		try {
			//apparently there are weird captive portals making use of query args on post requests
			//URL postUrl = new URL (url.getProtocol() + "://" + url.getAuthority() + url.getPath());
			URL postUrl = url;
			context.debug("Post URL = [" + postUrl + "]");
			conn = (HttpURLConnection) postUrl.openConnection();
			conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			if (cookies != null)
				conn.setRequestProperty("Cookie", cookies);
			conn.setRequestMethod("POST");
			
			DataOutputStream output = new DataOutputStream (conn.getOutputStream());
			output.writeBytes(postDataString);
			output.flush();
			output.close();
			context.debug("Data posted, checking result ...");
			return receive(context, conn);
		}catch ( ProtocolException e)
		{
			context.exception (e);
			return null;
		}catch (FileNotFoundException e)
		{
			context.debug("Can't read result - FileNotFound exception.");
			return null;
		} catch (IOException e) {
			context.exception (e);
			return null;
		}finally {
			conn.disconnect();
		}
	}
	
	public static ParsedHttpInput get (Worker context, URL url) {
		context.debug("Getting [" + url + "]");
		if (url != null){
			try {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
				conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
				conn.setUseCaches(false);
				return receive (context, conn);
			} catch (IOException e) {
				context.exception(e);
			}
		}
		return null;
	}

	public static ParsedHttpInput receive (Worker creator, HttpURLConnection conn) {
		ParsedHttpInput parsed = null;
		int totalBytesIn = 0;
		try {
			InputStream in = null;
			in = new BufferedInputStream(conn.getInputStream());

			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			char []buffer = new char[4096];
			int bytesIn;
			while ((bytesIn = r.read(buffer, 0, 4096)) >= 0) {
				if (bytesIn > 0) {
					total.append(buffer, 0, bytesIn);
					totalBytesIn += bytesIn;
				}
			}
			parsed = new ParsedHttpInput(creator, conn.getURL(), total.toString());
		}catch (IOException e) {
			creator.error ("Failed to receive from " + conn.toString());
			creator.exception (e);
		} catch (Throwable e) {
			creator.exception (e);
		}
		if (parsed != null) {
			creator.debug("Read " + totalBytesIn + " bytes:");
			creator.debug (parsed.getHtml());
	    
			int pos = 0;
			String field = null;
			do {
				field = conn.getHeaderField(pos);
				if (field != null) {
					creator.debug("Field["+pos+"("+conn.getHeaderFieldKey(pos)+")] = [" + field + "]");
				}
				++pos;
			}while (field != null);
		}
	    return parsed;
	}

}
