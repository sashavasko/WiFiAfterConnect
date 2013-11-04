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

import java.net.MalformedURLException;
import java.net.URL;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 * Handles Captive Portals by Motorolla Solutions Inc. (WiNG5.x)
 * See reference : http://support.symbol.com/support/search.do?cmd=displayKC&docType=kc&externalId=72E-170137-01apdf&sliceId=&dialogID=29747960&stateId=0%200%2029745257
 * and http://support.symbol.com/support/search.do?cmd=displayKC&docType=kc&externalId=WING5XHowToCaptivePortalsRevApdf&sliceId=&dialogID=29749091&stateId=0%200%2029745275 
 * 
 * Portal's page has a form by name and id of "frmLogin" that requires 2 hidden inputs to be set from the query string : 
 * f_hs_server  from hs_server=
 * f_Qv   from Qv=
 * The third hidden input f_agree has no apparent meaning.
 * 
 * May also have a checkbox by name "agree" that has to be set to value=Yes
 * Default form's action is "/cgi-bin/hslogin.cgi", but apparently it gets updated by the javascript like so :
 * 
 * var postToUrl = "/cgi-bin/hslogin.cgi";
 * var port = 880;
 * hs_server = getQueryVariable("hs_server");
 * postToUrl = ":" + port + postToUrl;
 * document.getElementById("frmLogin").action = "http://" + hs_server + postToUrl;
 *  
 * Typically hs_server is the same as Captive Portal
 * Qv is a long string of parameters concatenated by @
 * 
 * Unlike normal query params hs_server and Qv are separated by '?' like so :
 * http://custacc.lowesstore.com:880/Store-Guest/agreement.html?hs_server=custacc.lowesstore.com?Qv=it_qpmjdz=Tupsf.Hvftu@bbb_qpmjdz=@dmjfou_njou=2:63:9667@dmjfou_nbd=51.1F.96.3C.1G.5D
 * 
 * Don't know if it all is clever or mad.
 *  
 */
public class WiNGHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	private String qV = "";
	private String hsServer = "";
	
	private static final String QUERY_VAR_REGEX = "[?]";
	private static final String QUERY_VAR_QV = "Qv";
	private static final String QUERY_VAR_HS_SERVER = "hs_server";
	private static final String LOGIN_FORM_NAME = "frmLogin";

	@Override
	public void setPage(HttpInput page) {
		super.setPage(page);
		qV = "";
		hsServer = "";

		try {
			for (String v : page.getURL().getQuery().split(QUERY_VAR_REGEX)) {
				if (v.startsWith(QUERY_VAR_QV))
					qV = v.substring(3);
				else if (v.startsWith(QUERY_VAR_HS_SERVER))
					hsServer = v.substring(10);
			}
		}catch (NullPointerException e) { // don't care
		}
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		// just in case
		return checkUsernamePasswordMissing (params);
	}

	@Override
	public Boolean detect(HttpInput page) {
		boolean hasQv = false;
		boolean hasHsServer = false;
		boolean hasFrmLogin = false;
		try {
			for (String v : page.getURL().getQuery().split(QUERY_VAR_REGEX)) {
				if (v.startsWith(QUERY_VAR_QV))
					hasQv = true;
				else if (v.startsWith(QUERY_VAR_HS_SERVER))
					hasHsServer = true;
			}
		}catch (NullPointerException e) {
			return false;
		}
		hasFrmLogin = (HtmlPage.getForm (page, LOGIN_FORM_NAME)!=null);
		return (hasQv && hasHsServer && hasFrmLogin);
	}

	@Override
	public HtmlForm getLoginForm() {
		return HtmlPage.getForm(page, LOGIN_FORM_NAME);
	}

	@Override
	public URL getPostURL() {
		// TODO rewrite with values from port/postToURL javascript vars?
		URL formPostURL = super.getPostURL();
		try {
			return new URL (formPostURL.getProtocol(), hsServer, formPostURL.getPort(), formPostURL.getFile());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			return formPostURL;
		}
	}

	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		form.setInputValue ("f_" + QUERY_VAR_QV, qV); 
		form.setInputValue ("f_" + QUERY_VAR_HS_SERVER, hsServer);
		form.setInputValue ("agree", "Yes");
		//form.setInputValue ("f_agree", "Yes");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "WiNG{hs_server={" +hsServer + "}, qV={" + qV + "}}";
	}

}
