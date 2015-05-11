package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class ColubrisTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://wireless.colubris.com:8080/index.asp";
	}

	@Override
	public String getInput() {
		final String html = 
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">"+
"	<head>"+
"		<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"+
"		<meta http-equiv=\"Expires\" content=\"0\" />"+
"		<meta http-equiv=\"Cache-Control\" content=\"no-cache\" />"+
"		<meta http-equiv=\"Pragma\" content=\"no-cache\" />"+
"		<title>Internet Access - Login Page</title>"+
"		<link rel=\"stylesheet\" type=\"text/css\" href=\"/style.css\"/>"+
"		<script type=\"text/javascript\" language=\"JavaScript\" src=\"/prototype.js\"></script>"+
"		<script type=\"text/javascript\" language=\"JavaScript\" src=\"/sessionwindow.js\"></script>"+
"		<script type=\"text/javascript\" language=\"JavaScript\" src=\"/setfocus.js\"></script>"+
"		<script type=\"text/javascript\" language=\"JavaScript\">"+
"		<!--"+
"			function showsessionpageifnotsubscribe(){"+
"				var action = $F('access_type');"+
"				var showSessionPage = false;"+
"				if (typeof(action) == 'string') {	if ($F('access_type') != 'subscribe') {		showSessionPage = true;	}} else {	showSessionPage = true;	}"+
"				if (showSessionPage) {showsessionpage('http://wireless.colubris.com:8080/session.asp');	}	}"+
"		//-->	</script>"+
		
" </head>"+
"	<body onload=\"javascript:setfocus();\">"+
	
"	<div class=\"wrapper\">"+
"	<p><b>Internet Access - Login Page</b><p>"+
"		<img src=\"logo.gif\">	<br />	<br />"+
"		<textarea id=\"temrs\" readonly=\"readonly\" rows=\"12\" name=\"terms\" cols=\"80\" >TERMS, CONDITIONS AND NOTICES blah blah blah			   </textarea>"+
"		<div id=\"main\">"+
"			<!-- Display error or warning messages -->"+
"			<div id=\"message\"></div>"+
		
"			<!-- Display login form -->"+
"			<div class=\"boxed\">"+
"				<form action=\"/goform/HtmlLoginRequest\" method=\"post\" id=\"loginForm\">"+
"					<input type=\"hidden\" name=\"error_url\" value=\"/index.asp\" />"+
"					<input type=\"hidden\" name=\"success_url\" value=\"http://wireless.colubris.com:8080/transport.asp\" />"+
"					<input type=\"hidden\" name=\"original_url\" value=\"http://149.20.20.135/debian/pool/\" />"+
"					<input type=\"hidden\" name=\"subscription_url\" value=\"https://wireless.colubris.com:8090/subscribe.asp\" />"+
"					<input type=\"hidden\" name=\"valid_fields\" value=\"access_type username password\" />"+

"					<table width=\"100%\" border=\"0\"  cellspacing=\"0\" cellpadding=\"0\">"+
"						<tr>"+
"							<td><input type=\"radio\" id=\"access_type\" name=\"access_type\" value=\"free_access\" checked=\"checked\" /></td>"+
"							<td>Use Free Access service</td>"+
"						</tr>"+
"						<tr class=\"hidedata\">"+
"							<td><input type=\"radio\" id=\"access_type\" name=\"access_type\" value=\"login\" /></td>"+
"							<td>Existing User</td>"+
"						</tr>"+
"						<tr>"+
"							<td>&nbsp;</td>"+
"							<td>"+
"								<table class=\"hidedata\" border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"4\">"+
"									<tr>"+
"										<td align=\"right\"><label for=\"username\">Username:</label></td>"+
"										<td><input type=\"text\" name=\"username\" maxlength=\"127\" size=\"25\" value=\"\"/></td>"+
"									</tr>"+
"									<tr>"+
"										<td align=\"right\"><label for=\"password\">Password:</label></td>"+
"										<td><input type=\"password\" name=\"password\" maxlength=\"127\" size=\"25\" /></td>"+
"									</tr>"+
"								</table>"+
"							</td>"+
"						</tr>"+
"					</table>"+

"					<hr/>"+

"					<table border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"4\">"+
"						<tr>"+
"							<td></td>"+
"							<td align=\"right\"><input type=\"submit\" name=\"login\" id=\"loginButton\" value=\"I Agree, Access the Internet\" /></td>"+
"						</tr>"+
"					</table>"+
"				</form>"+
"			</div>"+

"			<div><p><a href=\"https://wireless.colubris.com:8090/index.asp\">Use the secure version of this form</a></p></div>"+

"		</div>"+
"		</div>"+
"	</body>"+
"</html>";
		return html;
	}

	@Override
	public void getHeaders(Map<String,String> headers) {
		headers.put("Date","Sun, 10 Nov 2013 00:59:21 GMT");
		headers.put("Server","GoAhead-Webs");
		headers.put("Pragma","no-cache");
		headers.put("Cache-Control","no-cache");
		headers.put("Content-length","9604");
		headers.put("Content-type","text/html");
		headers.put("X-Android-Sent-Millis","1384045153686");
		headers.put("X-Android-Received-Millis","1384045153715");
	}

	@Override
	public WifiAuthParams getParams() {
		// should not need no input
		return null;
	}

	@Override
	public String getPostURL() {
		// TODO Auto-generated method stub
		return "http://wireless.colubris.com:8080/goform/HtmlLoginRequest";
	}

	@Override
	public String getPostData() {
		// TODO Auto-generated method stub
		return "error_url=%2Findex.asp&success_url=http%3A%2F%2Fwireless.colubris.com%3A8080%2Ftransport.asp"+
		"&original_url=http%3A%2F%2F149.20.20.135%2Fdebian%2Fpool%2F"+
		"&subscription_url=https%3A%2F%2Fwireless.colubris.com%3A8090%2Fsubscribe.asp"+
		"&valid_fields=access_type+username+password" +
		"&access_type=free_access"+
		"&username="+
		"&password="+
		"&login=I+Agree%2C+Access+the+Internet"+
		"";
	}

	@Override
	public String getMetaRefresh() {
		return null;
	}

}
