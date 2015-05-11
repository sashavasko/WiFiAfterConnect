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

import com.wifiafterconnect.ParsedHttpInput;
import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.handlers.CaptivePageHandler.States;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.html.JavaScript;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 *
 */
public class HiltonHandler extends CaptivePageHandler implements CaptivePageHandler.Detection {
	
	private String folioAction;
	private String folioHash;
	private String folioPm;
	private String hiltonHonorsAction;
	private String hiltonHonorsHash;
	private String hiltonHonorsPm; 
	private String onsubmit;
	
	@Override
	public void setPage(HttpInput page) {
		super.setPage(page);
		final String signature = "var HiltonHonorsAction =";
		JavaScript js = ((HtmlPage)page).getHeadJavaScript (signature);
		if (js != null) {
			folioAction = js.evalStringVar ("FolioAction"); 
			folioHash = js.evalStringVar ("FolioHash");
			folioPm = js.evalStringVar ("FolioPm");
			hiltonHonorsAction = js.evalStringVar ("HiltonHonorsAction");
			hiltonHonorsHash = js.evalStringVar ("HiltonHonorsHash");
			hiltonHonorsPm = js.evalStringVar ("HiltonHonorsPm");
		}

		if (folioAction != null) {
			HtmlForm form = getLoginForm();
			form.setAction (folioAction);
			form.setInputValue("ValidationHash", folioHash);
			form.setInputValue("PaymentMethod", folioPm);
			form.setInputValue("compTier", "1"); 
		}else if (hiltonHonorsAction != null) {
			HtmlForm form = getLoginForm();
			form.setAction (hiltonHonorsAction);
			form.setInputValue("ValidationHash", hiltonHonorsHash);
			form.setInputValue("PaymentMethod", hiltonHonorsPm); 
		} 
	}

	
	@Override
	public Boolean detect(HttpInput page) {
		HtmlForm form = HtmlPage.getForm(page);
		if (form == null)
			return false;
		onsubmit = form.getOnsubmit();
		return onsubmit.contains("validateHiltonLogin")
			   || onsubmit.contains("validatePMSForm");
	}

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,	WifiAuthParams authParams) {
		ParsedHttpInput result = parsedPage.postForm (authParams);
		setState (result!=null ? States.Success : States.Failed);
		return result;
	}

}
