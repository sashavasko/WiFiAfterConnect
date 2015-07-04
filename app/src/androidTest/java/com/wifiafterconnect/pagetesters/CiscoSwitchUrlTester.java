package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class CiscoSwitchUrlTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://1.1.1.1/fs/customwebauth/login.html?switch_url=http://1.1.1.1/login.html&ap_mac=58:97:1e:57:5b:50&client_mac=d0:22:be:14:9e:ff&wlan=SLAM_GUEST&redirect=149.20.4.71/debian/pool/";
	}

	@Override
	public String getInput() {
		// TODO Auto-generated method stub
		return "<html><head><meta http-equiv=\"Pragma\" content=\"no-cache\"><meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=iso-8859-1\">"+
"<title>Welcome to St. Louis Art Museum</title>" +
"<script>function submitAction(){"+
"      var link = document.location.href;"+
"      var searchString = \"redirect=\";"+
"      var equalIndex = link.indexOf(searchString);"+
"      var redirectUrl = \"\";"+

"      if (document.forms[0].action == \"\") {"+
"      var url = window.location.href;"+
"      var args = new Object();"+
"      var query = location.search.substring(1);"+
"      var pairs = query.split(\"&\");"+
"          for(var i=0;i<pairs.length;i++){"+
"              var pos = pairs[i].indexOf('=');"+
"              if(pos == -1) continue;"+
"              var argname = pairs[i].substring(0,pos);"+
"              var value = pairs[i].substring(pos+1);"+
"              args[argname] = unescape(value);"+
"          }"+
"          document.forms[0].action = args.switch_url;"+       
"      }       "+
"      if(equalIndex >= 0) {"+
"            equalIndex += searchString.length;"+
"            redirectUrl = \"\";"+
"            redirectUrl += link.substring(equalIndex);"+
"      }"+
"      if(redirectUrl.length > 255)"+
"      redirectUrl = redirectUrl.substring(0,255);"+
"      document.forms[0].redirect_url.value = redirectUrl;"+
"      document.forms[0].buttonClicked.value = 4;"+
"      document.forms[0].submit();"+
"}"+

"function reject(){	alert(\"You will not be able to access the system!\");}"+

"function loadAction(){"+
"      var url = window.location.href;"+
"      var args = new Object();"+
"      var query = location.search.substring(1);"+
"      var pairs = query.split(\"&\");"+
"      for(var i=0;i<pairs.length;i++){"+
"          var pos = pairs[i].indexOf('=');"+
"          if(pos == -1) continue;"+
"          var argname = pairs[i].substring(0,pos);"+
"          var value = pairs[i].substring(pos+1);"+
"          args[argname] = unescape(value);"+
"      }   document.forms[0].action = args.switch_url;}</script>"+
"</head><body bgcolor=#FFEECC topmargin=\"50\" marginheight=\"50\" onload=\"loadAction();\"> " +
"<form method=\"post\"> " +
"<input TYPE=\"hidden\" NAME=\"buttonClicked\" SIZE=\"16\" MAXLENGTH=\"15\" value=\"0\"> " +
"<input TYPE=\"hidden\" NAME=\"redirect_url\" SIZE=\"255\" MAXLENGTH=\"255\" VALUE=\"\"> " +
"<input TYPE=\"hidden\" NAME=\"err_flag\" SIZE=\"16\" MAXLENGTH=\"15\" value=\"0\"> " +

"<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">" +

"<h1 align=center><font color=\"#336699\">Welcome to St. Louis Art Museum</font></h1>" +
"<iframe src=\"./aup.html\" width=\"800\" height=\"500\" scrolling=\"auto\"></iframe>" +
"<p><input type=\"button\" name=\"Submit\" value=\"Accept\" class=\"button\" onclick=\"submitAction();\"></p>"+
"<p><input type=\"button\" name=\"Reject\" value=\"Reject\" class=\"button\" onclick=\"reject();\"></p>"+
"<IMG SRC=\"./yourlogo.jpg\"><p></p></td></tr></table></div>"+

"</form>"+
"</body></html>";
	}

	@Override
	public void getHeaders(Map<String,String> headers) {
		headers.put("Date","Sun, 10 Nov 2013 20:14:49 GMT");
		headers.put("Connection","close");
		headers.put("Content-Length","2755");
		headers.put("Content-Type","text/html");
		headers.put("Last-Modified","Mon, 04 Mar 2013 20:54:28 GMT");
		headers.put("Cache-Control","public,max-age=3600");
		headers.put("X-Android-Sent-Millis","1384114489693");
		headers.put("X-Android-Received-Millis","1384114489726");
	}

	@Override
	public WifiAuthParams getParams() {
		// no user/password needed
		return null;
	}

	@Override
	public String getPostURL() {
		return "http://1.1.1.1/login.html";
	}

	@Override
	public String getPostData() {
		return "buttonClicked=4&redirect_url=149.20.4.71%2Fdebian%2Fpool%2F&err_flag=0&Submit=Accept&Reject=Reject";
	}

	@Override
	public String getMetaRefresh() {
		return null;
	}

}
