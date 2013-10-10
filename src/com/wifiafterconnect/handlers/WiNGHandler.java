/**
 * 
 */
package com.wifiafterconnect.handlers;

import java.net.URL;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlPage;

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
public class WiNGHandler extends CaptivePageHandler {

	public WiNGHandler(URL url, HtmlPage page) {
		super(url, page);
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#getPostData(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public String getPostData(WifiAuthParams params) {
		// TODO Auto-generated method stub
		return null;
	}

}
