/**
 * 
 */
package com.wifiafterconnect.handlers;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlForm;
import com.wifiafterconnect.util.HttpInput;

/**
 * @author sasha
 *
 */
public class AttHandler extends CaptivePageHandler  implements CaptivePageHandler.Detection{

	public static final String SIGNATURE = "AT&T Wi-Fi";
	
	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#checkParamsMissing(com.wifiafterconnect.WifiAuthParams)
	 */
	@Override
	public boolean checkParamsMissing(WifiAuthParams params) {
		// ATT wi-fi hotspots require no user entry, just click the Continue button
		return false;
	}

	/* (non-Javadoc)
	 * @see com.wifiafterconnect.handlers.CaptivePageHandler#validateLoginForm(com.wifiafterconnect.WifiAuthParams, com.wifiafterconnect.html.HtmlForm)
	 */
	@Override
	public void validateLoginForm(WifiAuthParams params, HtmlForm form) {
		// Nothing to enter and nothing to validate
	}

	@Override
	public Boolean detect(HttpInput page) {
		return page.getTitle().equalsIgnoreCase(SIGNATURE);
	}

}
