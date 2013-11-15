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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.wifiafterconnect.handlers.CaptivePageHandler;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.html.WISPAccessGatewayParam;
import com.wifiafterconnect.util.HttpInput;
import com.wifiafterconnect.util.Preferences;
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
	private Map<String,String> httpHeaders;
	
	public static final String HTTP_HEADER_LOCATION = "Location";
	// Yes, we are dirty liars
	public static final String HTTP_USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:24.0) Gecko/20100101 Firefox/24.0";
	public static final String HTTP_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	

	public ParsedHttpInput (Worker other, URL url, String html, Map<String,String> headers) {
		super (other);
		httpHeaders = headers;
		parse (url, html);
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
			debug("Detected page handler "+ captiveHandler);
		
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
	
	public HtmlForm getHtmlForm () {
		HtmlPage hp = getHtmlPage();
		return  hp != null ? hp.getForm() : null;
	}
	
	public String buildPostData (final WifiAuthParams authParams) {
		if (captiveHandler != null)
			return captiveHandler.getPostData(authParams);
		HtmlForm form = getHtmlForm ();
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

	public ParsedHttpInput handleAutoRedirects (int maxRequests, boolean authFollowup) {

		debug ("handleAutoRedirects(): this = " + this + " max Requests = " + maxRequests + " authFollowup = " + authFollowup);
		if (maxRequests <= 0 
				|| (Preferences.getWISPrEnabled() && getWISPr() != null)) // per WISPr 2.0 spec 7.3.3
			return this;
		
		ParsedHttpInput result = null;

		String redirectLoc = null;
		if (submitOnLoad() || (authFollowup && hasSubmittableForm())) {
			debug("Handling onLoad submit ...");
			result = postForm (null);
		}else if (!(redirectLoc = getHttpHeader(ParsedHttpInput.HTTP_HEADER_LOCATION)).isEmpty()){
			debug("Handling Location redirect ...");
			result = getRefresh (redirectLoc);
		}else if (hasMetaRefresh() && (authFollowup || getHtmlForm () == null)) {
			debug("Handling meta refresh ...");
			result = getRefresh (null);
		}else
			return this;
		
		if (result != null) 
			result = result.handleAutoRedirects(maxRequests-1, authFollowup);
		debug ("handleAutoRedirects(): result = " + result);
		return result; 
	}
	
	public ParsedHttpInput authenticateCaptivePortal(WifiAuthParams authParams) {
		return (captiveHandler != null) ? captiveHandler.authenticate (this, authParams) : null;
	}
	
	public CaptivePageHandler.States getCaptivePortalState () {
		return (captiveHandler != null)? captiveHandler.getState() : CaptivePageHandler.States.Failed;
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

	public HtmlPage.MetaRefresh getMetaRefresh() {
		HtmlPage hp = getHtmlPage();
		return hp != null ? hp.getMetaRefresh() : null;
	}
	
	public boolean isKnownCaptivePortal() { 
		return (captiveHandler != null);
	}

	/* ###################################################### 
	 * The Captive Portal check code from android. Unlike them,
	 * we actually need the portal page, so that we can post a response.
	 * 
	 * Copyright (C) 2012 The Android Open Source Project
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 */
	public static InetAddress lookupHost(String hostname) {
        InetAddress inetAddress[];
        try {
            inetAddress = InetAddress.getAllByName(hostname);
        } catch (UnknownHostException e) {
            return null;
        }

        for (InetAddress a : inetAddress) {
            if (a instanceof Inet4Address) return a;
        }
        return null;
    }
	/* ======================================================
	 * End of the The Captive Portal check code from android
	 */
	
	public URL getFormPostURL() {
		return (captiveHandler != null) ? captiveHandler.getPostURL() : getURL();
	}

	public WISPAccessGatewayParam getWISPr() {
		HtmlPage hp = getHtmlPage();
		return (hp != null) ? hp.getWISPr() : null;
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
	
	public ParsedHttpInput getRefresh (String urlString) {
		URL url = null;
		if (httpInput != null) {
			try {
				HtmlPage.MetaRefresh metaRefresh;
				if (urlString != null)
					url = httpInput.makeURL(urlString);
				else if ((metaRefresh = getMetaRefresh()) != null){
					url = metaRefresh.getURL();
					if (url != null && metaRefresh.getTimeout() > 0) {
						try {	Thread.sleep(1000 * Math.min(metaRefresh.getTimeout(),Constants.MAX_REFRESH_TIMEOUT)); } 
						catch (InterruptedException e) {} // don't care
					}
				}
			} catch (MalformedURLException ee) {
				exception(ee);
			}
		}
		return url == null ?  null : get (this, url, getURL().toString());
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

	public static ParsedHttpInput post (Worker context, URL url, String postDataString, String cookies, String referer) {
		context.debug("POST to url  [" + url + "]");
		context.debug("data to post ["+postDataString+"]");
		if (postDataString == null || postDataString.isEmpty()){
			context.error("Failed to compile data for authentication POST");
			return null;
		} else if (url == null) {
			context.error("Missing URL to POST the form");
			return null;
		}

		HttpURLConnection conn = null;
		try {
			URL postURL = url;
			/*
			int port = url.getPort(); 
			if (port > 0 && port < 1000 && port != 80 && port != 443) 
			{
				InetAddress ip = lookupHost(url.getHost());
				if (ip != null) {
					try {
						postURL = new URL (url.getProtocol(), ip.getHostAddress(), port, url.getFile());
					} catch (MalformedURLException e) {
						context.exception (e);
					}
				}
			}
			 */
			context.debug("Post URL = [" + postURL + "]");
			conn = (HttpURLConnection) postURL.openConnection();
			conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			
			// we need the next two to avoid 404 code on non-standard ports : 
			conn.setRequestProperty("User-Agent",HTTP_USER_AGENT);
			conn.setRequestProperty("Accept", HTTP_ACCEPT);
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			//conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataString.getBytes().length));
			conn.setRequestProperty("Origin", url.getProtocol() + "://" + url.getHost() + ":" + url.getPort());
			conn.setRequestProperty("Connection", "close");
			if (referer != null) {
				conn.setRequestProperty("Referer",referer);
			}
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
				conn.setRequestProperty("User-Agent",HTTP_USER_AGENT);
				conn.setRequestProperty("Accept","*/*");
				conn.setRequestProperty("Connection", "close");
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

	private static void showReceivedConnection (Worker context, HttpURLConnection conn, String data, int totalBytesIn, Map <String,String> headers) {
		for (String key : headers.keySet())
			context.debug("Field["+ key + "] = [" + headers.get(key) + "]");

		context.debug ("Read " + totalBytesIn + " bytes:");
		if (context.isSaveLogToFile()) {
			context.debug (data);
			context.debug ("#####################");
		}
    
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
			
			Map<String,String> headers = new HashMap<String,String>();
			String field = null;
			for (int pos = 0 ; (field = conn.getHeaderField(pos)) != null ; ++pos )
				headers.put (conn.getHeaderFieldKey(pos), field);
			
			if (BuildConfig.DEBUG)
				showReceivedConnection (creator, conn, total.toString(), totalBytesIn, headers);
			
			parsed = new ParsedHttpInput(creator, conn.getURL(), total.toString(), headers);
		}catch (IOException e) {
			creator.error ("Failed to receive from " + conn.toString());
			creator.exception (e);
		} catch (Throwable e) {
			creator.exception (e);
		}
	    return parsed;
	}

}
