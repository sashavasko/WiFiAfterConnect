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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.nodes.Element;

import android.text.InputType;

public class HtmlInput {
	public static final String TYPE_BUTTON = "button";
	public static final String TYPE_CHECKBOX = "checkbox";
	public static final String TYPE_COLOR = "color";
	public static final String TYPE_DATE = "date";
	public static final String TYPE_DATETIME = "datetime";
	public static final String TYPE_DATETIME_LOCAL = "datetime-local";
	public static final String TYPE_EMAIL = "email";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_HIDDEN = "hidden";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_MONTH = "month";
	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_PASSWORD = "password";
	public static final String TYPE_RADIO = "radio";
	public static final String TYPE_RANGE = "range";
	public static final String TYPE_RESET = "reset";
	public static final String TYPE_SEARCH = "search";
	public static final String TYPE_SUBMIT = "submit";
	public static final String TYPE_TEL = "tel";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_TIME = "time";
	public static final String TYPE_URL = "url";
	public static final String TYPE_WEEK = "week";

	public static final String DEFAULT_TYPE = TYPE_TEXT;
	public static final String CHARSET_NAME = "UTF-8";
	
	// Empty strings if not available (except type)(see Jsoup docs : http://jsoup.org/apidocs/org/jsoup/nodes/Node.html#attr%28java.lang.String%29)
	private String name;
	private String type = DEFAULT_TYPE;
	private String value;
	private String inputClass="";
	private String onClick="";
	private String form="";
	private String checked="";
	private boolean forceHidden = false;

	public void setType (String type) {
		this.type = (type == null || type.isEmpty() ? DEFAULT_TYPE : type);
	}
	
	public HtmlInput (final String name, final String type, final String value) {
		this.name = name == null ? "" : name;
		setType (type);
		this.value = value == null ? "" : value;
	}
	
	public HtmlInput (Element e, boolean hidden) {
		name = e.attr("name");
		setType (e.attr("type"));
		forceHidden = hidden;
		value = e.attr("value");
		inputClass = e.attr("class");
		onClick = e.attr("onClick");
		form = e.attr("form");
		checked = e.attr("checked");
	}

	public HtmlInput (HtmlInput other) {
		name = other.name;
		type = other.type;
		forceHidden = other.forceHidden;
		value = other.value;
		inputClass = other.inputClass;
		onClick = other.onClick;
		form = other.form;
		checked = other.checked;
	}

	public boolean isHidden() {
		return forceHidden || type.equalsIgnoreCase(TYPE_HIDDEN);
	}
	
	public boolean isValid() {
		return (name != null && !name.isEmpty());
	}
	
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public String getValue() {
		return value;
	}
	
	public boolean matchType(String type) {
		return this.type.equalsIgnoreCase(type);
	}
	
	public String getFormId() {
		return form;
	}
	public void setValue (String value) {
		this.value = (value == null) ? "" : value;
	}
	public String getOnClick() {
		return onClick;
	}
	
	public boolean isClass (String inputClass) {
		return (inputClass != null && this.inputClass.equals(inputClass));
	}
	
	public StringBuilder formatPostData (StringBuilder postData) {
		// TODO: Using URLEncoder is probably less efficient then UriBuilder
		try {
			if (matchType (HtmlInput.TYPE_IMAGE)) {
				postData.append('&').append('x').append('=').append('1');
				postData.append('&').append('y').append('=').append('1');
			}else if (matchType (HtmlInput.TYPE_RADIO) && checked.isEmpty()) {
				// ignore unchecked radio buttons
			}else
				postData.append('&').append(URLEncoder.encode(name,CHARSET_NAME)).append('=').append(URLEncoder.encode(value,CHARSET_NAME));
		} catch (UnsupportedEncodingException e) {
			// should never get here
		}
		return postData;
	}
	
	public int getAndroidInputType () {
    	if (matchType (HtmlInput.TYPE_PASSWORD))
    		return InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD;
    	else if (matchType (HtmlInput.TYPE_EMAIL))
    		return InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    	else if (matchType (TYPE_TEXT))
    		return InputType.TYPE_CLASS_TEXT;
    	else if (matchType (TYPE_NUMBER))
    		return InputType.TYPE_CLASS_NUMBER;
    	else if (matchType (TYPE_TEL))
    		return InputType.TYPE_CLASS_NUMBER;
    	else if (matchType (TYPE_DATE))
    		return InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_DATE;
    	else if (matchType (TYPE_DATETIME) |matchType (TYPE_DATETIME_LOCAL))
    		return InputType.TYPE_CLASS_DATETIME;
    	else if (matchType (TYPE_TIME))
    		return InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_TIME;
    	return 0;
	}

	@Override
	public String toString() {
		return "<input name=\"" + name + 
				"\" type=\"" + (isHidden() ? TYPE_HIDDEN : type) + 
				"\" value=\"" + value + "\" class=\"" + inputClass + 
				"\" onClick=\"" + onClick + "\" form=\"" + form + 
				"\" checked=\"" + checked + "\">";
	}
	
}