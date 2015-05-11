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

import com.wifiafterconnect.BuildConfig;
import com.wifiafterconnect.Constants;
import com.wifiafterconnect.WifiAuthParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HtmlForm {
	// Empty strings if not available (see Jsoup docs : http://jsoup.org/apidocs/org/jsoup/nodes/Node.html#attr%28java.lang.String%29) 
	protected String id;
	protected String action;
	protected String method;
	protected String onsubmit;
	protected URL actionURL;
	protected Map<String, HtmlInput> inputs = new HashMap<String,HtmlInput>();
	// This will store inputs in the same order they are found in original HTML
	protected ArrayList<HtmlInput> inputsList = new ArrayList<HtmlInput>();
	
	public void addInput (HtmlInput i) {
		if (i != null && i.isValid()) {
			inputs.put (i.getName(), i);
			inputsList.add (i);
		}
	}
	
	HtmlForm (Element e) {
		id = e.attr("id");
		setAction(e.attr("action"));

		method = e.attr("method");
		onsubmit = e.attr("onsubmit");
				
    	for (Element ie : e.getElementsByTag("input")) {
    		if (BuildConfig.DEBUG)
    			Log.d(Constants.TAG, "Parsing html: form input found : " + ie.toString());
    		boolean hidden = false;
    		for (Element parent = ie.parent(); parent != e ; parent = parent.parent())
    			// this weirdness is used in Colubris (now owned by HP) portals:
    			if (parent.hasClass("hidedata")|| parent.hasClass("hidden")) {
    				hidden = true;
    				break;
    			}
    		addInput(new HtmlInput (ie, hidden));
    	}
	}
	
	public String getOnsubmit() {
		return onsubmit;
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
		for (HtmlInput i :inputsList) {
			i.formatPostData(postData);
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
				String origPath = originalURL.getPath(); //ignore query of original url 
				if (file.isEmpty())
					file = origPath;
				else if (!file.startsWith("/")) {
					file = origPath.substring(0, origPath.lastIndexOf('/')+1) + file;
				}
					
				protocol = originalURL.getProtocol();
			}
			if (authority == null)
				authority = originalURL.getAuthority();
			
			String urlString = protocol + "://" + authority;
			if (file != null)
				urlString += file;
			if (ref != null)
				urlString += "#" + ref;
			if (BuildConfig.DEBUG)
				Log.d(Constants.TAG, "actionURL string = [" + urlString + "]");
			try {
				result = new URL (urlString);
			} catch (MalformedURLException e) {	}
		}else {
			// TODO need to check for onclick="form.action=" in type="submit" inputs
		}
		return result;
	}
	
	public WifiAuthParams fillParams (WifiAuthParams params) {
		
		for (HtmlInput i :inputsList) {
			if (!i.isHidden() && !params.hasParam(i.getName())) {
				HtmlInput param = new HtmlInput (i);
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
			if (!i.isHidden() && i.getValue().isEmpty()
					&& (WifiAuthParams.isSupportedParamType(i) || i.matchType(HtmlInput.TYPE_CHECKBOX)))
				missingValues = true;
			if (i.matchType(HtmlInput.TYPE_SUBMIT))
				hasSubmit = true;
		}		
		return !missingValues && hasSubmit;
	}

	public void setAction(String switchUrl) {
		action = (switchUrl == null)? "" : switchUrl;
		try {
			actionURL = new URL(action);
		} catch (MalformedURLException ex) {actionURL = null;}
	}

}
