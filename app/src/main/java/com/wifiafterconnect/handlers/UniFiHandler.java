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

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

public class UniFiHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	/*
	 * http://community.ubnt.com/unifi
	 * Example page : http://community.ubnt.com/t5/UniFi/Payment-Page-iDevices-Still-not-selecting-right-item/td-p/527891
	 * This portal includes options for paying, we will only support the free variations for obvious reasons.
	 * Tricky part is that it has 2 forms. Once button is clicked on first form the second form with TOU becomes visible
	 * which has checkbox for accepting TOU.
	 * 
	 */
	public static final int LOGIN_FORM = 0;
	

	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return checkUsernamePasswordMissing (params);
	}

	@Override
	public Boolean detect(HttpInput page) {
		return (HtmlPage.getForm(page, LOGIN_FORM) != null && HtmlPage.getForm(page, LOGIN_FORM).hasInputWithClass("button requires-tou"));
	}

	@Override
	public HtmlForm getLoginForm() {
		return HtmlPage.getForm(page, LOGIN_FORM);
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// Should not need anything
	}
}
