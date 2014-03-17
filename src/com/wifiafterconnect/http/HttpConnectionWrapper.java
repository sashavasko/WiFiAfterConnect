/*
 * Copyright (C) 2014 Sasha Vasko <sasha at aftercode dot net> 
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
package com.wifiafterconnect.http;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.wifiafterconnect.util.Worker;

/**
 * @author Sasha Vasko
 *
 */
public abstract class HttpConnectionWrapper {
	private URL url;
	protected Map<String,String> headers = new HashMap<String,String>();
	protected String data;
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public Map<String,String> getHeaders() {
		return headers;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}
	
	public void reset() {
		headers.clear();
		url = null;
		data = null;
	}

	public void showReceived (Worker context) {
		context.debug ("Page received:");
		for (String key : headers.keySet())
			context.debug("Field["+ key + "] = [" + headers.get(key) + "]");

		if (data != null) {
			context.debug ("Read " + data.length() + " bytes:");
			if (context.isSaveLogToFile()) {
				context.debug (data);
				context.debug ("#####################");
			}
		}
	}
	
	public abstract boolean post (Worker context, String postDataString, String cookies, String referer);
	public abstract boolean get (Worker context, String referer);
	
}
