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
import com.wifiafterconnect.handlers.CaptivePageHandler.Detection;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 * Handler for NetNearU (nnu.com) portals. These portals use completely insane redirection schema
 * They require posting of initial parameters via form like most others, but instead of using standard 
 * submit() javascript, they use custom function init(), that also set's window size  into w and h inputs. 
 */
public class NNUHandler extends CaptivePageHandler implements Detection {
	
	public static final String DEFAULT_WIDTH = "640";
	public static final String DEFAULT_HEIGHT = "480";

	boolean preLoginForm = false;
	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler.Detection#detect(com.wifiafterconnect.util.HttpInput)
	 */
	@Override
	public Boolean detect(HttpInput input) {
		return (input.getURL().getHost().contains("nnu.com") && HtmlPage.getForm(input) != null);
	}

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,
			WifiAuthParams authParams) {
		ParsedHttpInput result = super.authenticate(parsedPage, authParams);

		if (result != null)
			setState (preLoginForm ? States.HandleRedirects : States.Success);
		else
			setState (States.Failed);
		return result;
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		super.validateLoginForm(params, form);
		if (form.hasInput("w") && form.hasInput("h")){
			preLoginForm = true;
			form.setInputValue("w", DEFAULT_WIDTH);
			form.setInputValue("h", DEFAULT_HEIGHT);
		}else {
			form.setInputValue("acceptterms", "on");
		}
		
	}

}
