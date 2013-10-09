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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

import com.wifiafterconnect.Constants;

public class HtmlPage {
	private String source = "";
	private Map<String,HtmlForm> namedForms = new HashMap<String,HtmlForm>();
	private List<HtmlForm> forms = new ArrayList<HtmlForm>();
	
	private String onLoad = "";
	private String metaRefresh = "";
	
	public boolean parse (String html) {
		this.source = html;
		
		Log.d(Constants.TAG, "Page " + this);
		
		if (html == null || html.isEmpty())
			return false;
		
		Document doc = Jsoup.parse(html);
		if (doc == null) {
			Log.d(Constants.TAG, "Parsing html: doc == null");
		  	return false;
		}    
		  
		Element content = doc.getElementById("content");
		if (content == null) {
			content = doc;
		}

		for (Element meta : content.getElementsByTag("meta")) {
			if (meta.attr("http-equiv").equalsIgnoreCase("refresh")) {
				metaRefresh = meta.attr("contents");
				break;
			}
		}

		for (Element body : content.getElementsByTag("body")) {
			Log.d(Constants.TAG, "Parsing html: body found.");
			if (body.hasAttr("onLoad")) {
				onLoad = body.attr("onLoad");
				break;
			}
		}
		
		for (Element fe : content.getElementsByTag("form")) {
			HtmlForm f = new HtmlForm (fe);
			forms.add (f);
			Log.d(Constants.TAG, "Parsing html: form added. Forms == " + forms.toString());
			String fid = f.getId();
			if (!fid.isEmpty())
				namedForms.put(fid, f);
		}
		
    	for (Element ie : content.getElementsByTag("input")) {
    		HtmlInput i = new HtmlInput (ie);
    		String fid = i.getFormId();
    		if (!fid.isEmpty()) {
    			HtmlForm f = namedForms.get(fid);
    			if (f != null)
    				f.addInput (i);
    		}
    	}
		
		return true;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getOnLoad() {
		return onLoad;
	}
	
	// For consistency sake both getForm methods return null if key is bad and don't throw an exception
	public HtmlForm getForm (int id) {
		HtmlForm f = null;
		try {
			f = forms.get(id);
		}catch (IndexOutOfBoundsException e) 
		{
			Log.d(Constants.TAG, "Page " + this + ". Index out of bounds retrieving the form #" + id + ". Forms == " + forms.toString());
		}
		return f;
	}
	
	public HtmlForm getForm (String id) {
		HtmlForm f = null;
		if (id != null)
			f = namedForms.get(id);
		return f;
	}
	
	public HtmlForm getForm () {
		return getForm(0);
	}

	public Collection<HtmlForm> forms () {
		return this.forms;
	}

	public boolean hasMetaRefresh() {
		return !metaRefresh.isEmpty();
	}
	
	public URL getMetaRefreshURL () throws MalformedURLException {
		if (metaRefresh.isEmpty())
			return null;
		
		URL url = null;
		int start = metaRefresh.indexOf("url=");
		if (start >= 0)
			url = new URL (metaRefresh.substring(start+4));
		
		return url;
	}
}
