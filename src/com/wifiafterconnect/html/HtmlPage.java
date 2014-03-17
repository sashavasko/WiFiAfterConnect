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
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import android.util.Log;
import com.wifiafterconnect.Constants;
import com.wifiafterconnect.util.HttpInput;

public class HtmlPage extends HttpInput {
	private Map<String,HtmlForm> namedForms = new HashMap<String,HtmlForm>();
	private List<HtmlForm> forms = new ArrayList<HtmlForm>();
	private List<JavaScript> headJavaScripts = new ArrayList<JavaScript>();
	private List<JavaScript> bodyJavaScripts = new ArrayList<JavaScript>();
	
	private String onLoad = "";
	private String title = "";
	private WISPAccessGatewayParam wISPr = null;
	
	private Map<String,String> namedMetas = new HashMap<String,String>();
	private Map<String,String> httpEquivMetas = new HashMap<String,String>();
	
	public class MetaRefresh {
		private int timeout = 0;
		private String url = null;
		
		public MetaRefresh (String source) {
			int start = source.toLowerCase(Locale.ENGLISH).indexOf("url=");
			if (start >= 0)
				url = source.substring(start+4);
			if (start > 2) {
				int end = source.indexOf(';');
				if (end > 0)
					timeout = Integer.parseInt(source.substring(0, end));
			}
		}
		
		public int getTimeout () {
			return timeout;
		}
		
		public String getURLString () {
			return url == null ? "" : url;
		}

		public URL getURL () throws MalformedURLException {
			return makeURL (url);
		}
	}
	
	
	public HtmlPage (URL url){
		super (url);
	}
	
	@Override
	public String getTitle () {
		return title;
	}

	protected boolean isJavaScript (Element jse){
		if (!jse.tag().toString().equals("script"))
			return false;
		String attr = "";
		if (jse.hasAttr("language"))
			attr = jse.attr("language");
		else if (jse.hasAttr("type"))
			attr = jse.attr("type");
		return attr.equalsIgnoreCase("javascript") || attr.equalsIgnoreCase("text/javascript");
	}
	
	@Override
	public boolean parse (String html) {
		Log.d(Constants.TAG, "Page " + this);
		if (!super.parse(html))
			return false;
		
		Document doc = Jsoup.parse(html);
		if (doc == null) {
			Log.d(Constants.TAG, "Parsing html: doc == null");
		  	return false;
		}    
		Log.d(Constants.TAG, "Parsing html: doc html == {" + doc.html()  + "}");

		// some portals sneak form to outside of <div id="content"> - the bastards!
		Element content = doc;/*.getElementById("content");
		if (content == null) {
			content = doc;
		}*/

		for (Element meta : content.getElementsByTag("meta")) {
			String c = meta.attr("content");
			if (!c.isEmpty()) {
				if (meta.hasAttr("http-equiv"))
					httpEquivMetas.put(meta.attr("http-equiv").toLowerCase(Locale.ENGLISH), c);
				else if (meta.hasAttr("name"))
					namedMetas.put(meta.attr("name").toLowerCase(Locale.ENGLISH), c);
			}
		}

		for (Element te : content.getElementsByTag("title")) {
			title = te.data();
			if (!title.isEmpty())
				break;
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
		for (Element head : content.getElementsByTag("head")) {
			for (Element jse : head.getElementsByTag("script")) {
				if (isJavaScript(jse)){
					JavaScript j = new JavaScript (jse);
					headJavaScripts.add (j);
					Log.d(Constants.TAG, "Parsing html: HEAD JS added. javaScripts = " + headJavaScripts.toString());
				}
			}
			if (!headJavaScripts.isEmpty())
				checkJavaScriptForMetaRefresh();
		}
		for (Element body : content.getElementsByTag("body")) {
			for (Element jse : body.getElementsByTag("script")) {
				if (isJavaScript(jse)){
					JavaScript j = new JavaScript (jse);
					bodyJavaScripts.add (j);
					Log.d(Constants.TAG, "Parsing html: HEAD JS added. javaScripts = " + bodyJavaScripts.toString());
				}
			}
		}

		for (Element ie : content.getElementsByTag("input")) {
    		HtmlInput i = new HtmlInput (ie, false);
    		String fid = i.getFormId();
    		if (!fid.isEmpty()) {
    			HtmlForm f = namedForms.get(fid);
    			if (f != null)
    				f.addInput (i);
    		}
    	}
		
		for(Element e : doc.getAllElements()){
	        for(Node n: e.childNodes()){
	            if(n instanceof Comment){
	            	String commentData = ((Comment)n).getData(); 
	            	if (commentData.startsWith("<?xml")) {
	            		WISPAccessGatewayParam wp = WISPAccessGatewayParam.parse(commentData);
	            		if (wp != null)
	            			wISPr = wp;
	            	}
	            }
	        }
	    }
		
		return true;
	}
	
	public JavaScript getHeadJavaScript (final String signature) {
		for (JavaScript js : headJavaScripts) 
			if (js.matchCode(signature) >= 0) 
				return js;
		return null;
	}
	
	private void checkJavaScriptForMetaRefresh() {
		// This is specific to GuestNetInc portals :
		final String signature = "document.write('<meta http-equiv=\"REFRESH\" content=\"0;url=' + cpUrl";
		if (getHeadJavaScript (signature) != null) {
			JavaScript js = getHeadJavaScript ("cpUrl =");
			Log.d(Constants.TAG, "checkJavaScriptForMetaRefresh(): js for cpUrl = " + js);

			if (js != null) {
				String cpUrl = js.evalStringVar ("cpUrl");
				Log.d(Constants.TAG, "checkJavaScriptForMetaRefresh(): cpUrl = " + cpUrl);
				if (!cpUrl.isEmpty()){
					String pAction = js.evalStringVar ("pAction");
					String controllerType = js.evalStringVar ("ControllerType");
					String hotelGroup = js.evalStringVar ("HotelGroup");
					String hotelBrand = js.evalStringVar ("HotelBrand");
					String hotelId = js.evalStringVar ("HotelId");
					String usr = js.evalStringVar ("usr");
					String pwd = js.evalStringVar ("pwd");

					cpUrl += "pa=" + pAction;
					if (!controllerType.isEmpty())
						cpUrl += "&ct=" + controllerType;
					if (!hotelGroup.isEmpty())
						cpUrl += "&hg=" + hotelGroup;
					if (!hotelBrand.isEmpty())
						cpUrl += "&hb=" + hotelBrand;
					if (!hotelId.isEmpty())
						cpUrl += "&id=" + hotelId;
					if (!usr.isEmpty())
						cpUrl += "&usr=" + usr;
					if (!pwd.isEmpty())
						cpUrl += "&pwd=" + pwd;

					httpEquivMetas.put("refresh", "0;url=" + cpUrl);
				}
			}
		}
	}
	
	public String getOnLoad() {
		return onLoad;
	}

	public WISPAccessGatewayParam getWISPr() {
		return wISPr;
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
	
	public static HtmlForm getForm(HttpInput page, int id) {
		return (page != null && (page instanceof HtmlPage)) ? ((HtmlPage)page).getForm(id) : null;
	}

	public HtmlForm getForm (String id) {
		HtmlForm f = null;
		if (id != null)
			f = namedForms.get(id);
		return f;
	}
	
	public static HtmlForm getForm(HttpInput page, String id) {
		return (page != null && id != null && (page instanceof HtmlPage)) ? ((HtmlPage)page).getForm(id) : null;
	}

	public HtmlForm getForm () {
		return getForm(0);
	}
	
	public static HtmlForm getForm(HttpInput page) {
		return (page != null && (page instanceof HtmlPage)) ? ((HtmlPage)page).getForm() : null;
	}

	public Collection<HtmlForm> forms () {
		return this.forms;
	}

	public boolean hasMetaRefresh() {
		return httpEquivMetas.containsKey("refresh");
	}
	
	public MetaRefresh getMetaRefresh () {
		String metaRefresh = httpEquivMetas.get ("refresh");
		return (metaRefresh != null)? new MetaRefresh(metaRefresh) : null ;
	}
	
	public String getMeta (final String name) {
		String c = namedMetas.get(name);
		return (c == null) ? "" : namedMetas.get(name);
	}
	
	public String getDocumentReadyFunc () {
		String func = null;
		for (JavaScript js : headJavaScripts) {
			if ((func = js.getDocumentReadyFunc()) != null)
				return func;
		}
		return func;
	} 
	
	public boolean hasFormWithInputType (String type) {
		for (HtmlForm f : forms) {
			if (f.getVisibleInputByType(type) != null)
				return true;
		}
		return false;
	}

	public boolean hasSubmittableForm() {
		for (HtmlForm f : forms) {
			if (f.isSubmittable())
				return true;
		}
		return false;
	}

}
