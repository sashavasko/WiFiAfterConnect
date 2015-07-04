/**
 * 
 */
package com.wifiafterconnect.handlers;

import com.wifiafterconnect.ParsedHttpInput;
import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.html.HtmlPage;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 *
 */
public class SwitchURLHandler extends CaptivePageHandler implements CaptivePageHandler.Detection{

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#validateLoginForm(com.wifiafterconnect.WifiAuthParams, com.wifiafterconnect.html.HtmlForm)
	 */
	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// no forms here, simply redirect to switch_url
	}

	@Override
	public Boolean detect(HttpInput page) {
		return HtmlPage.getForm(page) == null && page.getURLQueryVar("switch_url") != null;
	}

	@Override
	public ParsedHttpInput authenticate(ParsedHttpInput parsedPage,
			WifiAuthParams authParams) {
		ParsedHttpInput result = parsedPage.getRefresh(parsedPage.getURLQueryVar("switch_url"));
		if (result != null && result.hasForm())
			result = null; // something went wrong
		setState (result!=null ? States.Success : States.Failed);
		return result;
	}

}
