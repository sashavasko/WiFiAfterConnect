package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;
import com.wifiafterconnect.handlers.NNUHandler;

public class MikrotikTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://redir.nnu.com/?hwid=00:0C:42:AF:4E:B1&clientip=10.59.0.168&loginpageurl=https://rap.nnu.com/login&url=http%3A%2F%2F149.20.4.71%2Fdebian%2Fpool%2F&usermac=D0:22:BE:14:9E:FF";
	}

	@Override
	public String getInput() {
		return 
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">" +
		"<html>" +
		"<head>" +
		"  <title>Redirecting...</title>" +
		"  <link rel=\"shortcut icon\" href=\"/media/images/favicon.ico\">" +
		"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
		" <script type=\"text/javascript\"> "+
		"  function init(){" +
		"		document.getElementById(\"content\").style.display=\"none\";" +
		"		check_cookies()?(" +
		"			document.getElementById(\"w\").value=screen.width," +
		"			document.getElementById(\"h\").value=screen.height," +
		"			document.getElementById(\"pf\").submit())" +
		"			:document.location=\"cookie.html\"" +
		"	}" +
		"	function check_cookies(){" +
		"	} " +
		" </script>" +
		"  <style type=\"text/css\">body {font-family: Arial, Verdana, Helvetica, sans-serif;}</style>" +
		"</head>" +
		"<body onload=\"init()\">" +
		  
		  
		"  <!--" +
		"  <?xml version=\"1.0\"><carrier_data><page_type>login</page_type><device_type>mikrotik</device_type><login_url>https://wireless.nnu.com/nw4/sites/templatet/free/</login_url><carrier_id>NNU</carrier_id><version>0</version><note>Login Page</note></carrier_data>" +
		"  <WISPAccessGatewayParam><Redirect><MessageType>100</MessageType><ResponseCode>0</ResponseCode><AccessProcedure>1.0</AccessProcedure>" +
		"<LocationName>NNU</LocationName><AccessLocation>18350</AccessLocation>" +
		"<LoginURL>https://wireless.nnu.com/nw4/sites/templatet/free/</LoginURL>" +
		"<AbortLoginURL>https://rap.nnu.com/logout</AbortLoginURL></Redirect></WISPAccessGatewayParam>" +
		"  -->" +

		"  <form id=\"pf\" name=\"pf\" action=\"/post/\" method=\"post\" accept-charset=\"utf-8\">" +
		"    <input type=\"hidden\" id=\"w\" name=\"w\" value=\"0\">" +
		"    <input type=\"hidden\" id=\"h\" name=\"h\" value=\"0\">" +
		"      <input type=\"hidden\" name=\"url\" value=\"http%3A%2F%2F149.20.4.71%2Fdebian%2Fpool%2F\">" +
		"      <input type=\"hidden\" name=\"loginpageurl\" value=\"https://rap.nnu.com/login\">" +
		"      <input type=\"hidden\" name=\"hwid\" value=\"00:0C:42:AF:4E:B1\">" +
		"      <input type=\"hidden\" name=\"sysid\" value=\"18350\">" +
		"      <input type=\"hidden\" name=\"usermac\" value=\"D0:22:BE:14:9E:FF\">" +
		"      <input type=\"hidden\" name=\"clientip\" value=\"10.59.0.168\">" +
		"  </form>" +
		"  <div id=\"content\">" +
		"    <h2>JavaScript is Required</h2>" +
		"    <div>You must have JavaScript enabled to properly view this website.<br>Click <a href=\"js.html\">here</a> for instructions to enable JavaScript for your browser<br></div>" +
		"  </div>" +
		"</body>" +
		"</html>"
;
	}

	@Override
	public void getHeaders(Map<String, String> headers) {
		headers.put("X-Android-Received-Millis","1384455992512");
		headers.put("Transfer-Encoding","chunked");
		headers.put("Connection","close");
		headers.put("Content-Type","text/html");
		headers.put("Date","Thu, 14 Nov 2013 19:06:33 GMT");
		headers.put("Server","NNUAS/1.0.0 WSGI Server");
		headers.put("X-Android-Sent-Millis","1384455992425");
	}

	@Override
	public WifiAuthParams getParams() {
		return null;
	}

	@Override
	public String getPostURL() {
		return "http://redir.nnu.com/post/";
	}

	@Override
	public String getPostData() {
		return "w=" + NNUHandler.DEFAULT_WIDTH +
				"&h=" + NNUHandler.DEFAULT_HEIGHT +
				"&url=http%253A%252F%252F149.20.4.71%252Fdebian%252Fpool%252F" +
				"&loginpageurl=https%3A%2F%2Frap.nnu.com%2Flogin" +
				"&hwid=00%3A0C%3A42%3AAF%3A4E%3AB1" +
				"&sysid=18350" +
				"&usermac=D0%3A22%3ABE%3A14%3A9E%3AFF" +
				"&clientip=10.59.0.168";
	}

	@Override
	public String getMetaRefresh() {
		return null;
	}

}
