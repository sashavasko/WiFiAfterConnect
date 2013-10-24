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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.wifiafterconnect.handlers.CaptivePageHandler;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;
import com.wifiafterconnect.util.Worker;

public class ParsedHttpInput extends Worker{

	public class JsonInput extends HttpInput {
		private JSONObject json = null;
		
		public JsonInput(URL url) {
			super(url);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean parse(String source) {
			json = null;
			if (!super.parse(source))
				return false;
			try {
				json = new JSONObject (source);
			} catch (JSONException e) {
			}
			return (json != null);
		}
		
		public JSONObject getJSONObject () {
			return json;
		}

		public JSONObject getJSONObject (final String name) throws JSONException {
			return json != null? json.getJSONObject(name) : null;
		}
	}
	
	private CaptivePageHandler captiveHandler = null;
	private HttpInput httpInput = null;
	private Map<String,String> httpHeaders = new HashMap<String,String>();
	
	public static final String HTTP_HEADER_LOCATION = "Location";
	

	public ParsedHttpInput (Worker other, URL url, String html) {
		super (other);
		parse (url, html);
	}

	private void addHttpHeader (final String key, final String value) {
		httpHeaders.put (key, value);
	}

	private void parse(URL url, String input) {
		captiveHandler = null;
		httpInput = null;
		httpHeaders.clear();
		
		if (input.startsWith("{")) {
			// TODO implement parsing as JSON???
			JsonInput jsonInput = new JsonInput (url);
			if (!jsonInput.parse(input))
				error("Failed to parse JSON input");
			else {
				httpInput = jsonInput;
				captiveHandler = CaptivePageHandler.autodetect (httpInput);
			}
		}else {
			HtmlPage htmlPage = new HtmlPage(url);
			if (!htmlPage.parse (input)) {
				error("Failed to parse the HTML page");
			}else {
				httpInput = htmlPage;
				if (!submitOnLoad(htmlPage)) {
					// Probably the actual login page
					captiveHandler = CaptivePageHandler.autodetect (httpInput);
				}
			}
		}

		if (captiveHandler != null)
			debug("Detected Captive portal "+ captiveHandler);
		
		// Fallback - plain text container
		if (httpInput == null){
			httpInput = new HttpInput (url);
			httpInput.parse(input);
		}
	}

	public HtmlPage getHtmlPage() {
		return (httpInput != null && httpInput instanceof HtmlPage) ? (HtmlPage)httpInput : null;
	}
	
	public JSONObject getJSONObject() {
		return (httpInput != null && httpInput instanceof JsonInput) ? ((JsonInput)httpInput).getJSONObject() : null;
	}
	
	public JSONObject getJSONObject(final String name) throws JSONException {
		return (httpInput != null && httpInput instanceof JsonInput) ? ((JsonInput)httpInput).getJSONObject(name) : null;
	}
	
	/*
	 * Guaranteed not to return null
	 */
	public String getHttpHeader (final String key) {
		String val = httpHeaders.get(key); 
		return val == null ? "" : val;
	}
	
	public String buildPostData (final WifiAuthParams authParams) {
		if (captiveHandler != null)
			return captiveHandler.getPostData(authParams);
		HtmlPage hp = getHtmlPage();
		HtmlForm form = hp != null ? hp.getForm() : null;
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

	public ParsedHttpInput handleAutoRedirects (int maxRequests, boolean handleMetaRefresh) {

		if (maxRequests <= 0)
			return this;
		
		ParsedHttpInput result = null;

		String redirectLoc = null;
		if (submitOnLoad() || hasSubmittableForm()) {
			debug("Handling onLoad submit ...");
			result = postForm (null);
		}else if (!(redirectLoc = getHttpHeader(ParsedHttpInput.HTTP_HEADER_LOCATION)).isEmpty()){
			debug("Handling Location redirect ...");
			result = getRefresh (redirectLoc);
		}if (handleMetaRefresh && hasMetaRefresh()) {
			debug("Handling meta refresh ...");
			result = getRefresh (null);
		}else
			return this;
		return (result != null) ? result.handleAutoRedirects(maxRequests-1, handleMetaRefresh) : null; 
	}
	
	public boolean authenticateCaptivePortal(WifiAuthParams authParams) {
		if (captiveHandler != null)
			return (captiveHandler.authenticate (this, authParams) != null);
		return false;
	}
	
	public boolean submitOnLoad(HtmlPage hp) {
		if (hp != null) {
			String onLoad = hp.getOnLoad();
			debug("OnLoad = [" + onLoad + "]");
			return onLoad.equalsIgnoreCase("document.forms[0].submit();")
					|| onLoad.equalsIgnoreCase("document.form.submit();")
					|| onLoad.endsWith(".submit();"); // this one is probably enough
		}
		return false;
	}
	
	public boolean submitOnLoad() {
		return submitOnLoad(getHtmlPage());
	}

	public final String getHtml() {
		return (httpInput != null && httpInput instanceof HtmlPage) ? httpInput.getSource() : "";
	}

	public final String getRaw() {
		return (httpInput != null) ? httpInput.getSource() : "";
	}

	public final URL getURL() {
		return (httpInput != null) ? httpInput.getURL() : null;
	}

	public boolean hasMetaRefresh() {
		HtmlPage hp = getHtmlPage();
		return hp != null && hp.hasMetaRefresh();
	}

	public URL getMetaRefreshURL() throws MalformedURLException {
		HtmlPage hp = getHtmlPage();
		return hp != null ? hp.getMetaRefreshURL() : null;
	}
	
	public boolean isKnownCaptivePortal() { 
		return (captiveHandler != null);
	}

	public URL getFormPostURL() {
		HtmlPage hp = getHtmlPage();
		if (hp != null) {
			HtmlForm form = hp.getForm();
			if (form != null)
				return form.formatActionURL(getURL());
		}
		return getURL();
	}

	public String getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasSubmittableForm() {
		HtmlPage hp = getHtmlPage();
		return hp != null && hp.hasSubmittableForm();
	}

	public boolean hasForm() {
		HtmlPage hp = getHtmlPage();
		return (hp != null && hp.getForm() != null);
	}
	
	public String getURLQueryVar(String varName) {
		return httpInput.getURLQueryVar(varName);
	}

	public ParsedHttpInput postForm (WifiAuthParams authParams) {

		URL url = getFormPostURL();
		String postDataString = buildPostData(authParams);
		String cookies = getCookies();
		
		return post (this, url, postDataString, cookies, getURL().toString());
	}
	
	public ParsedHttpInput getRefresh (final String url) {
		ParsedHttpInput result = null;
		try {
			result = get (this, url == null ? getMetaRefreshURL() : new URL(url), getURL().toString());
		} catch (MalformedURLException e) {
			exception (e);
		}
		
		return result;
	}
	
	public static void  showRequestProperties (Worker context, HttpURLConnection conn) {
		Map<String,List<String>> reqProps = conn.getRequestProperties();
		context.debug("RequestProperties for [" + conn.getURL() + "]");
		for (String key : reqProps.keySet()) {
			String propStr = "RequestPropery[" + key + "] = {";
			for (String val : reqProps.get(key)) {
				propStr += "[" + val + "]";
			}
			context.debug(propStr + "}");
		}
	}
	// Yes, we are dirty liars
	public static final String USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:24.0) Gecko/20100101 Firefox/24.0";

	public static ParsedHttpInput post (Worker context, URL url, String postDataString, String cookies, String referer) {
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
			URL postURL = url;
			context.debug("Post URL = [" + postURL + "]");
			conn = (HttpURLConnection) postURL.openConnection();
			conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			
			// we need the next two to avoid 404 code on non-standard ports : 
			conn.setRequestProperty("User-Agent",USER_AGENT);
			conn.setRequestProperty("Accept","*/*");
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataString.getBytes().length));
			if (referer != null)
				conn.setRequestProperty("Referer",referer);
			if (cookies != null)
				conn.setRequestProperty("Cookie", cookies);
			
			showRequestProperties (context, conn);
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
	
	public static ParsedHttpInput get (Worker context, URL url, String referer) {
		context.debug("Getting [" + url + "]");
		if (url != null){
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
				conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
				conn.setUseCaches(false);
				conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
				conn.setRequestProperty("Accept","*/*");
				if (referer != null)
					conn.setRequestProperty("Referer",referer);
				
				showRequestProperties (context, conn);
				return receive (context, conn);
			} catch (IOException e) {
				context.exception(e);
			}finally {
				conn.disconnect();
			}
		}
		return null;
	}
	
	public static ParsedHttpInput receive (Worker creator, HttpURLConnection conn) {
		ParsedHttpInput parsed = null;
		int totalBytesIn = 0;
		try {
			InputStream in = null;
			try {
				in = new BufferedInputStream(conn.getInputStream());
			} catch (FileNotFoundException e) {
				in = new BufferedInputStream(conn.getErrorStream());
			}

			creator.debug("Response code = " + conn.getResponseCode());
			
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
					parsed.addHttpHeader(conn.getHeaderFieldKey(pos), field);
					creator.debug("Field["+pos+"("+conn.getHeaderFieldKey(pos)+")] = [" + field + "]");
				}
				++pos;
			}while (field != null);
		}
	    return parsed;
	}

}
