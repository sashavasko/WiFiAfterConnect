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
import com.wifiafterconnect.html.HtmlInput;

/**
 * @author sasha
 *
 */
public class GenericHandler extends CaptivePageHandler {

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		WifiAuthParams allParams = addMissingParams(params);
		if (allParams != null) {
			for (HtmlInput i : allParams.getFields()) {
				if (i.getValue().isEmpty())
					return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#validateLoginForm(com.wifiafterconnect.WifiAuthParams, com.wifiafterconnect.html.HtmlForm)
	 */
	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		for (HtmlInput i : form.getInputs()) {
			if (!i.isHidden() && i.matchType(HtmlInput.TYPE_CHECKBOX) && i.getValue().isEmpty())
				i.setValue("yes");
		}
	}

}
