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
import com.wifiafterconnect.html.HtmlPage;

/**
 * @author sasha
 *
 */
public class GenericHandler extends CaptivePageHandler {

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,
			WifiAuthParams authParams) {
		ParsedHttpInput result = super.authenticate(parsedPage, authParams);

		// some portals throw up a page while authenticating user - 
		// must wait for the final page with the 0 timeout:
		
		HtmlPage.MetaRefresh metaRefresh;
		int count = 0 ;
		while (result != null 
				&& (metaRefresh = result.getMetaRefresh()) != null 
				&& metaRefresh.getTimeout() > 0) {
			result = result.getRefresh(null);
			count++;
		}
		// One last refresh to get to the actual location's landing place 
		if (count > 0 && result != null && (metaRefresh = result.getMetaRefresh()) != null)
			result = result.getRefresh(null);

		setState (result!=null ? States.Success : States.Failed);
		return result;
	}
	
	
}
