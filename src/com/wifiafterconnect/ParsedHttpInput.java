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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.wifiafterconnect.handlers.CaptivePageHandler;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.Logger;

public class ParsedHttpInput {

	private Logger logger;
	private CaptivePageHandler captiveHandler = null;
	private HtmlPage htmlPage;

	public ParsedHttpInput (Logger logger, URL url, String html) {
		this.logger = logger;
		parse (url, html);
	}

	public static ParsedHttpInput receive (Logger logger, HttpURLConnection conn) {
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
			parsed = new ParsedHttpInput(logger, conn.getURL(), total.toString());
		}catch (IOException e) {
			logger.error ("Failed to receive from " + conn.toString());
			logger.exception (e);
		} catch (Throwable e) {
			logger.exception (e);
		}
		if (parsed != null) {
			logger.debug("Read " + totalBytesIn + " bytes:");
			logger.debug (parsed.getHtml());
	    
			int pos = 0;
			String field = null;
			do {
				field = conn.getHeaderField(pos);
				if (field != null) {
					logger.debug("Field["+pos+"("+conn.getHeaderFieldKey(pos)+")] = [" + field + "]");
				}
				++pos;
			}while (field != null);
		}
	    return parsed;
	}
	
	public void parse(URL url, String html) {
		
		htmlPage = new HtmlPage(url);
		if (!html.isEmpty()) {
			if (!htmlPage.parse (html)) {
				if (logger != null)
					logger.error("Failed to parse the page - no document found");
				return;
			}

			if (!submitOnLoad()) {
				// Probably the actual login page
				captiveHandler = CaptivePageHandler.autodetect (htmlPage);
				if (logger != null && captiveHandler != null)
					logger.debug("Detected Captive portal "+ captiveHandler);
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
	
	public boolean submitOnLoad() {
		String onLoad = htmlPage.getOnLoad();
		if (logger != null)
			logger.debug("OnLoad = [" + onLoad + "]");
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
}
