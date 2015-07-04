package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class HiltonTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://nmd.hil-jefdtdt.kan.wayport.net/index.adp";
	}

	@Override
	public String getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getHeaders(Map<String, String> headers) {
		headers.put("MIME-Version","1.0");
		headers.put("Date","Thu, 13 Feb 2014 02:32:25 GMT");
		headers.put("Content-Length","15358");
		headers.put("Expires","now");
		headers.put("X-Android-Received-Millis","1392258745196");
		headers.put("Connection","close");
		headers.put("Content-Type","text/html; charset=UTF-8");
		headers.put("Server","AOLserver/4.5.1");
		headers.put("X-Android-Sent-Millis","1392258744854");
	}

	@Override
	public WifiAuthParams getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPostURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPostData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMetaRefresh() {
		// TODO Auto-generated method stub
		return null;
	}

}
