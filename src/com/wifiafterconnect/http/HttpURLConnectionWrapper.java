/*
 * Copyright (C) 2014 Sasha Vasko <sasha at aftercode dot net> 
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
package com.wifiafterconnect.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wifiafterconnect.BuildConfig;
import com.wifiafterconnect.Constants;
import com.wifiafterconnect.util.Worker;

/**
 * @author Sasha Vasko
 *
 */
public class HttpURLConnectionWrapper extends HttpConnectionWrapper{

	// Yes, we are dirty liars
	public static final String HTTP_USER_AGENT = "Mozilla/5.0 (Android; Mobile; rv:24.0) Gecko/20100101 Firefox/24.0";
	public static final String HTTP_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
	
	@Override
	public boolean post(Worker context, String postDataString, String cookies, String referer) {
		URL url = getUrl();
		context.debug("POST to url  [" + url + "]");
		context.debug("data to post ["+postDataString+"]");
		if (postDataString == null || postDataString.isEmpty()){
			context.error("Failed to compile data for authentication POST");
			return false;
		} else if (url == null) {
			context.error("Missing URL to POST the form");
			return false;
		}

		HttpURLConnection conn = null;
		
		try {
			URL postURL = url;
			context.debug("Post URL = [" + postURL + "]");
			conn = (HttpURLConnection) postURL.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setConnectTimeout(Constants.SOCKET_CONNECT_TIMEOUT_MS);
			conn.setReadTimeout(Constants.SOCKET_READ_TIMEOUT_MS);
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
		}catch (FileNotFoundException e)
		{
			context.debug("Can't read result - FileNotFound exception.");
		} catch (IOException e) {
			context.exception (e);
		}finally {
			if (conn != null)
				conn.disconnect();
		}
		return false;
	}

	@Override
	public boolean get(Worker context, String referer) {
		context.debug("Getting [" + getUrl() + "]");
		HttpURLConnection conn = null;
		if (getUrl() != null){

			try {
				conn = (HttpURLConnection) getUrl().openConnection();
				conn.setConnectTimeout(Constants.SOCKET_CONNECT_TIMEOUT_MS);
				conn.setReadTimeout(Constants.SOCKET_READ_TIMEOUT_MS);
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
				if (conn != null)
					conn.disconnect();
			}
		}
		return false;
	}
	
	private boolean receive (Worker context, HttpURLConnection conn) {
		try {
			InputStream in = null;
			try {
				in = new BufferedInputStream(conn.getInputStream());
			} catch (FileNotFoundException e) {
				in = new BufferedInputStream(conn.getErrorStream());
			}

			Map<String,String> headers = new HashMap<String,String>();
			String field = null;
			for (int pos = 0 ; (field = conn.getHeaderField(pos)) != null ; ++pos )
				headers.put (conn.getHeaderFieldKey(pos), field);

			if (BuildConfig.DEBUG)
				showReceived (context);
			
			context.debug("Response code = " + conn.getResponseCode());
			
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			char []buffer = new char[4096];
			int bytesIn;
			while ((bytesIn = r.read(buffer, 0, 4096)) >= 0) {
				if (bytesIn > 0) {
					total.append(buffer, 0, bytesIn);
				}
			}
			
			data = total.toString();
			if (BuildConfig.DEBUG)
				showReceived (context);

			return true;
		}catch (IOException e) {
			context.error ("Failed to receive from " + conn.toString());
			context.exception (e);
		} catch (Throwable e) {
			context.exception (e);
		}
	    return false;
	}

	private void  showRequestProperties (Worker context, HttpURLConnection conn) {
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


}
