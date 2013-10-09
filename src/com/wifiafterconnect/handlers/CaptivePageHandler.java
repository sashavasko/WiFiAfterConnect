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

package com.wifiafterconnect.handlers;

import java.net.URL;

import com.wifiafterconnect.Constants;
import com.wifiafterconnect.WifiAuthParams;

import android.util.Log;

import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;

public abstract class CaptivePageHandler {

	public static CaptivePageHandler autodetect (URL url, HtmlPage page) {
		//TODO: this needs to go into actual classes ?
		
		if (url.getHost().contains("wanderingwifi"))
			return new WanderingWifiHandler(url, page);
		
		HtmlForm form = page.getForm();
		if (form != null && form.hasInput("buttonClicked"))
			return new CiscoHandler(url, page);
		
		// This appears to be pretty unique :
		if (form != null && form.hasInputWithClass("button requires-tou"))
			return new UniFiHandler(url, page);

		return null;
	}
	
	
	protected URL originalUrl;
	protected HtmlPage page;
	
	public CaptivePageHandler (URL url, HtmlPage page) {
		this.originalUrl = url;
		this.page = page;
	}
	
	public abstract boolean checkParamsMissing (WifiAuthParams params);
	public abstract String getPostData (WifiAuthParams params);

	/* 
	 * Possibly to be overriden in subclasses
	 */
	public URL getPostUrl () 
	{ 
		HtmlForm form = page.getForm();
		if (form != null)
			return form.formatActionURL (originalUrl);
		return originalUrl; 
	};
	
	public boolean checkUsernamePasswordMissing (WifiAuthParams params, HtmlForm form){
		Log.d(Constants.TAG, "Checking for missing params. Form = " + form);
		return (form != null && form.isParamMissing(params, WifiAuthParams.USERNAME)||form.isParamMissing(params, WifiAuthParams.PASSWORD)); 
	}
	
	public WifiAuthParams addMissingParams (WifiAuthParams params) {
		HtmlForm form = page.getForm();
		if (form != null) 
			params = form.fillParams (params);
		return params;
	}
	
}
