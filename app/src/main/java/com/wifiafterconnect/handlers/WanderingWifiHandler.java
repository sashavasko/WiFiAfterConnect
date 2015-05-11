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

import com.wifiafterconnect.Constants;
import com.wifiafterconnect.ParsedHttpInput;
import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 *
 */
public class WanderingWifiHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthenticator.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return checkUsernamePasswordMissing (params);
	}

	@Override
	public Boolean detect(HttpInput input) {
		return (input.getURL().getHost().contains("wanderingwifi") && HtmlPage.getForm(input) != null);
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// TODO need to check onClick script for the Login Button
		form.setInputValue ("Action", "Login"); 
	}

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,
			WifiAuthParams authParams) {
		ParsedHttpInput result = super.authenticate(parsedPage, authParams);

		// Wandering WiFi is nuts. It has Several!!! automated pages at the end of authentication, 
		// using a mix of redirecting methods, allow meta http-equiv=refresh here - we can't rely on JS.
		if (result != null)
			result = result.handleAutoRedirects(Constants.MAX_AUTOMATED_REQUESTS, true);

		setState (result!=null ? States.Success : States.Failed);
		return result;
	}

}
