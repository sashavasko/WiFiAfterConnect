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

package com.wifiafterconnect.html;

import org.jsoup.nodes.Element;

import android.util.Log;

import com.wifiafterconnect.Constants;
import com.wifiafterconnect.WifiAuthParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HtmlForm {
	// Empty strings if not available (see Jsoup docs : http://jsoup.org/apidocs/org/jsoup/nodes/Node.html#attr%28java.lang.String%29) 
	protected String id;
	protected String action;
	protected String method;
	protected URL actionURL;
	protected Map<String, HtmlInput> inputs = new HashMap<String,HtmlInput>();
	
	public void addInput (HtmlInput i) {
		if (i != null && i.isValid())
			inputs.put (i.getName(), i);
	}
	
	HtmlForm (Element e) {
		id = e.attr("id");
		action = e.attr("action");
		try {
			actionURL = new URL(action);
		} catch (MalformedURLException ex) {actionURL = null;}

		method = e.attr("method");
    	for (Element ie : e.getElementsByTag("input")) {
    		Log.d(Constants.TAG, "Parsing html: form input found : " + ie.toString());
    		addInput(new HtmlInput (ie));
    	}
	}
	
	public String getMethod () {
		return method.toUpperCase(Locale.ENGLISH);
	}

	public String getAction () {
		return action;
	}
	
	public String getId () {
		return id;
	}
	
	public Collection<HtmlInput> getInputs() {
		return inputs.values();
	}
	
	public boolean hasInput (String name) {
		return inputs.containsKey(name);
	}

	public boolean hasInputWithClass (String classAttr) {
		for (HtmlInput i :inputs.values()) {
			if (i.isClass (classAttr))
				return true;
		}		
		return false;
	}

	public boolean hasVisibleInput (String name) {
		HtmlInput i = inputs.get(name);
		return (i != null && !i.isHidden());
	}

	public HtmlInput getInput (String name) {
		return inputs.get(name);
	}

	public HtmlInput getVisibleInput (String name) {
		HtmlInput i = getInput (name);
		return (i == null || i.isHidden()) ? null : i;
	}
	
	public HtmlInput getVisibleInputByType (String type) {
		for (HtmlInput i :inputs.values()) {
			if (!i.isHidden() && i.matchType (type))
				return i;
		}		
		return null;
	}

	public boolean setInputValue (String name, String value) {
		HtmlInput i = inputs.get(name);
		if (i != null)
			i.setValue (value);
		return inputs.containsKey(name);
	}
	
	public String formatPostData () {
		StringBuilder postData = new StringBuilder();
		for (HtmlInput i :inputs.values()) {
			i.formatPostData(postData.append('&'));
		}
		if (postData.length() > 0) {
			if (postData.charAt(0) == '&')
				postData.deleteCharAt(0);
		}
		return postData.toString();
	}
	
	public URL formatActionURL (URL originalURL) {
		URL result = originalURL;
		if (action != null) {
			String protocol;
			String authority = null;
			String file = action;
			String ref = null;
			if (actionURL != null) {
				protocol = actionURL.getProtocol();
				authority = actionURL.getAuthority();
				// we want to keep the query in as some portals use query params in post requests
				file = actionURL.getFile();
				ref = actionURL.getRef();
			}else {
				protocol = originalURL.getProtocol();
			}
			if (authority == null)
				authority = originalURL.getAuthority();
			
			String urlString = protocol + "://" + authority;
			if (file != null)
				urlString += file;
			if (ref != null)
				urlString += "#" + ref;

			try {
				result = new URL (urlString);
			} catch (MalformedURLException e) {	}
		}else {
			// TODO need to check for onclick="form.action=" in type="submit" inputs
		}
		return result;
	}
	
	public WifiAuthParams fillParams (WifiAuthParams params) {
		
		for (HtmlInput i :inputs.values()) {
			if (!i.isHidden()) {
				HtmlInput param = new HtmlInput (i);
				if (params == null)
					params = new WifiAuthParams();
				params.add(param);
			}
		}		
		return params;
	}
	
	public boolean isParamMissing (WifiAuthParams params, final String paramName) {
		return (hasVisibleInput(paramName) && (params == null || !params.hasParam (paramName)));
	}

	public void fillInputs (WifiAuthParams params) {
		if (params != null) {
			for (HtmlInput f :params.getFields()) {
				String value = f.getValue();
				HtmlInput i = inputs.get(f.getName());
				if (i!=null)
					i.setValue(value);
			}
		}
	}

	public boolean isSubmittable() {
		boolean missingValues = false;
		boolean hasSubmit = false;
		for (HtmlInput i :inputs.values()) {
			if (!i.isHidden() && i.getValue().isEmpty() && WifiAuthParams.isSupportedParamType(i))
				missingValues = true;
			if (i.matchType(HtmlInput.TYPE_SUBMIT))
				hasSubmit = true;
		}		
		return !missingValues && hasSubmit;
	}
	


}
