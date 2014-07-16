/*
 * Copyright (c) 2014 wvengen <dev-mobile@willem.engen.nl>
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
import com.wifiafterconnect.html.HtmlInput;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * Handler for NS WiFi (on Dutch trains)
 *
 * Known to work at: July 2014
 *
 * @author wvengen
 */
public class NSTreinHandler extends CaptivePageHandler implements CaptivePageHandler.Detection {

	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return false;
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// javascript link adds new parameter (and removes parameter "connectionForm_hf_0", but we don't)
		HtmlInput input = new HtmlInput("connectionLink", "hidden", "x");
		form.addInput(input);
	}

	@Override
	public Boolean detect(HttpInput input) {
		return (input.getURL().getHost().contains("nstrein.ns.nl") && HtmlPage.getForm(input) != null);
	}
}
