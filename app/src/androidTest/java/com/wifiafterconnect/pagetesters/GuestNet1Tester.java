package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class GuestNet1Tester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://10.0.0.1:8000/index.php?redirurl=http%3A%2F%2F149.20.4.71%2Fdebian%2Fpool%2F";
	}

	@Override
	public String getInput() {
		return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"+
"				<html>\n"+
"				<head>\n"+
"				<script type=\"text/javascript\">\n"+
"						var HotelBrand, HotelGroup, HotelId, cpUrl, ControllerType, pAction, usr, pwd, redir ;\n"+
"						pAction = \"http://10.0.0.1:8000/&err=\";\n"+
"				// Set manual variables below.\n"+	  
"						/* Controller Types\n"+
"							es = \"EthoStream\"\n"+
"							gn = \"Guestnet Gateway\"\n"+
"							mt = \"Microtik\"\n"+
"							nx = \"Nomadix\"\n"+
"							vp = \"ValuePoint\"\n"+
"						*/\n"+
"					// ControllerType = \"vp\";    // Set Controller Type Here\n"+
"						/* Hotel Brands\n"+
"							hi = \"Holiday Inn\"\n"+
"							hie = \"Holiday Inn Express\"\n"+
"							ms = \"Mainstay\"\n"+
"							cw = \"Candlewood\"\n"+
"							sb = \"Staybridge\"\n"+
"							ci = \"Comfort Inn\"\n"+
"							bw = \"Best Western\"\n"+
"							fp = \"Four Points by Sheraton\"\n"+
"							s8 = \"Super 8\"\n"+
"							*/\n"+
"					// HotelGroup = \"tr\";    // Manually Set Hotel Group Here\n"+
"					// HotelBrand = \"s8\";    // Manually Set Hotel Brand Here\n"+
"					// HotelId = \"20177\";    // Manually Set Hotel Code Here\n"+
"					 // usr = \"PC103\";    // Manually Set User Here\n"+
"					// pwd = \"yes\";    // Only use \"yes\" or \"no\" to manually indicate that guests need a pass code.\n"+
"						/* Use redir if you want to specify a manual redirect page.\n"+
"							e.g. \"http://www.wiscohotels.com/wiscobaymont/index.php\")\n"+
"							*/\n"+
"					// redir = \"\";\n"+    
"				// Do NOT change anything below this line!\n"+
"						cpUrl = \"http://login.guestnetinc.com/login.php?\";\n"+
"							cpUrl += \"pa=\" + pAction;\n"+
"						if (ControllerType){\n"+
"							cpUrl += \"&\" + \"ct=\" + ControllerType;\n"+
"						}\n"+
"						if (HotelGroup){\n"+
"							cpUrl += \"&\" + \"hg=\" + HotelGroup;\n"+
"						}\n"+
"						if (HotelBrand){\n"+
"							cpUrl += \"&\" + \"hb=\" + HotelBrand;\n"+
"						}\n"+
"						if (HotelId){\n"+
"							cpUrl += \"&\" + \"id=\" + HotelId;\n"+
"						}\n"+
"						if (usr) {\n"+
"							cpUrl += \"&\" + \"usr=\" + usr;\n"+
"						}\n"+
"						if (pwd) {\n"+
"							cpUrl += \"&\" + \"pwd=\" + pwd;\n"+
"						}\n"+
"						if (redir) {\n"+
"							cpUrl += \"&\" + \"redir=\" + redir;\n"+
"						}\n"+
"				</script>\n"+
"				<title>Login</title>\n"+
"				<script language=\"JavaScript\">\n"+
"				document.write('<meta http-equiv=\"REFRESH\" content=\"0;url=' + cpUrl + '\">');\n"+
"				</script>\n"+
"				</HEAD> \n"+
"				<BODY>\n"+
"				Please wait for Login Page . . .\n"+ 
"				</BODY>\n"+
"				</HTML>\n";
	}

	@Override
	public void getHeaders(Map<String, String> headers) {
		headers.put("Date","Fri, 15 Nov 2013 23:46:35 GMT");
		headers.put("Transfer-Encoding","chunked");
		headers.put("Content-type","text/html");
		headers.put("Expires","0");
		headers.put("X-Android-Received-Millis","1384559194137");
		headers.put("Connection","close");
		headers.put("Server","lighttpd/1.4.29");
		headers.put("X-Android-Sent-Millis","1384559193865");
		headers.put("Pragma","no-cache");
		headers.put("Cache-Control","post-check=0, pre-check=0");
	}

	@Override
	public WifiAuthParams getParams() {
		return null;
	}

	@Override
	public String getPostURL() {
		return null;
	}

	@Override
	public String getPostData() {
		return null;
	}

	@Override
	public String getMetaRefresh() {
		return "http://login.guestnetinc.com/login.php?pa=http://10.0.0.1:8000/&err=";
	}

}
