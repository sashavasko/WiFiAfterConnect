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

/**
 * @author sasha
 *
 */
public class CiscoHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	/*
	 * See CISCO docs at http://www.cisco.com/en/US/docs/wireless/controller/7.3/configuration/guide/b_wlc-cg_chapter_01011.html
	 */

	private String switchUrl = null;
	private String redirect = null;
	
	@Override
	public void setPage(HttpInput page) {
		super.setPage(page);
		switchUrl = page.getURLQueryVar("switch_url");
		redirect =  page.getURLQueryVar("redirect");
		if (switchUrl != null) {
			HtmlForm form = getLoginForm();
			form.setAction (switchUrl);
		}
	}

	
	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthenticator.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return checkUsernamePasswordMissing (params);
	}

	@Override
	public Boolean detect(HttpInput page) {
		HtmlForm form = HtmlPage.getForm(page);
		//Log.d(Constants.TAG, "Form = " + form + " Page is HTML = " + (page instanceof HtmlPage));
		return (form != null && form.hasInput("buttonClicked"));
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// this could be different if submitAction script altered from Cisco's sample :
		super.validateLoginForm(params, form);
		form.setInputValue ("buttonClicked", "4");
		if (redirect != null && !redirect.isEmpty())
			form.setInputValue ("redirect_url", redirect);
	}


}
