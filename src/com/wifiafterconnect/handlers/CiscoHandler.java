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

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;

/**
 * @author sasha
 *
 */
public class CiscoHandler extends CaptivePageHandler {
/*
 * See CISCO docs at http://www.cisco.com/en/US/docs/wireless/controller/7.3/configuration/guide/b_wlc-cg_chapter_01011.html
 */
	public CiscoHandler(URL url, HtmlPage page) {
		super(url, page);
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthenticator.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return checkUsernamePasswordMissing (params, page.getForm());
	}

	@Override
	public String getPostData(WifiAuthParams params) {
		HtmlForm form = page.getForm();
		if (form != null) {
			form.fillInputs(params);
			// this could be different if submitAction script altered from Cisco's sample :
			form.setInputValue ("buttonClicked", "4"); 
			return form.formatPostData();
		}
		return null;
	}


}
