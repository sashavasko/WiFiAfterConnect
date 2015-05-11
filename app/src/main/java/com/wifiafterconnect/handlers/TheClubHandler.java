/**
 * 
 */
package com.wifiafterconnect.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.wifiafterconnect.Constants;
import com.wifiafterconnect.ParsedHttpInput;
import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 * The Club - free wifi in many malls in US owned by GGP (ggp.com).
 * 
 * Welcome page implemented by Nearbuy Systems. 
 * Supposedly Nearbuy wifi will autoconnect users on repeat visits. Hopefully it works and will catch up with other vendors.
 * Apparently they integrate with WiNG5 by Motorolla as well, so this may need to be merged with WiNGHandler.
 * Then again in this particular case the hardware is by Aruba Networks.
 * http://www.arubanetworks.com/aruba-partners/ecosystem-partners/mobile-applications/
 * 
 * Uses custom jQuery script to login registered user by e-mail, 
 * and then authenticate to hotspot by arubanetworks through auth_url returned in json result.
 * First POST is submitted to /user_action with data : name=page_load&page=Welcome+Page
 * Followed by POST to /user_action with data: name=Log+in&page=Welcome+Page
 * Finally e-mail is submitted by POST to original url (including the query) with data: Email+Address=user%40domain.com
 * Tis return json like so: {"success":true,"redirect":"/connected.html","auth_url":"http://securelogin.arubanetworks.com:/auth/index.html/u?user=user_TOKEN&password=SECRET&command=authenticate&url=....","local_iframe_redirect":false}
 * At this point need to GET the url in auth_url field, which will return empty page with <meta http-equiv="refresh"> tag pointing to the test url.
 *
 * Chances are that user/password tokens in this url are the same for any user at certain location
 *
 * weird stuff.
 * 
 */
public class TheClubHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	public static final String LOGIN_FORM_ID = "login_form";
	public static final String USER_ACTION_PATH = "/user_action";
	
	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		HtmlForm form = getLoginForm();
		Log.d(Constants.TAG, "Checking for missing params. Form = " + form);
		return (form != null && form.isParamMissing(params, WifiAuthParams.EMAIL)); 
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#validateLoginForm(com.wifiafterconnect.WifiAuthParams, com.wifiafterconnect.html.HtmlForm)
	 */
	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// nothing to do here
	}

	@Override
	public Boolean detect(HttpInput page) {
		if (!(page instanceof HtmlPage))
			return false;
		HtmlPage hp = (HtmlPage)page;
		HtmlForm loginForm = HtmlPage.getForm(page, LOGIN_FORM_ID);
		if (loginForm == null)
			return false;
		if (!loginForm.hasInput("Email Address"))
			return false;
		
		HtmlForm signupForm = hp.getForm("signupform");
		if (signupForm == null)
			return false;
		
		if (!hp.getMeta("description").equalsIgnoreCase("Nearbuy Systems Welocome Page"))
			return false;
		
		return true;
	}

	@Override
	public HtmlForm getLoginForm() {
		return HtmlPage.getForm(page, LOGIN_FORM_ID);
	}

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,
			WifiAuthParams authParams) {
		String urlString = page.getURL().getProtocol() + "://" + page.getURL().getAuthority() + USER_ACTION_PATH;
		URL userActionURL;
		try {
			userActionURL = new URL (urlString);
		} catch (MalformedURLException e) {
			parsedPage.exception(e);
			return null;
		}
		
		String referer = page.getURL().toString();
		ParsedHttpInput.post(parsedPage, userActionURL, "name=page_load&page=Welcome+Page", null, referer);
		ParsedHttpInput.post(parsedPage, userActionURL, "name=Log+in&page=Welcome+Page", null, referer);
		ParsedHttpInput result = parsedPage.postForm (authParams);
		JSONObject json;
		if (result != null && (json = result.getJSONObject()) != null) {
			String authURL;
			try {
				authURL = json.getString("auth_url");
			} catch (JSONException e) {
				return null;
			}
			result = result.getRefresh (authURL); 
		}
		setState (result!=null ? States.Success : States.Failed);
		return result;
	}

}
