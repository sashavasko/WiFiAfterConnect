package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class WifiSoftTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "http://69.69.112.33:1111/usg/process";
	}

	@Override
	public String getInput() {

		return "<html>\n" +
"<head>\n" +
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\n" +
"<meta http-equiv=\"refresh\" content=\"1; URL=/usg/radius?OS=http://www.capitolplazajeffersoncity.com%2Fdining%2Ffountain-court-lounge&prefix=&Counter=1&RLF=\">\n" +
"<title>AG 5600: ISP Subscription Pending</title></head>\n" +
"<body bgcolor=\"white\">\n" +
"<br><center><img border=0 src=../image.gif>\n" +
"<table bgcolor=\"#E0E0C2\" border=0 cellPadding=0 cellSpacing=0 width=400>\n" +
"<tr><td>&nbsp;\n" +
"<table bgcolor=\"#E0E0C2\" border=0 cellPadding=1 cellSpacing=1 width=400>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableTag = '</table>';\n" +
"document.write(tableTag);\n" +
"// -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableEnd = '</tr></td>';\n" +
"document.write(tableEnd);\n" +
"// -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableTag = '</table>';\n" +
"document.write(tableTag);\n" +
"// -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- hide JavaScript from older browsers\n" +
"var defaultWidth = 400;\n" +
"var screenWidth = 800;\n" +
"var browser = navigator.userAgent.toLowerCase();\n" +
"if ((browser.indexOf('windows ce')>0)\n" +
"&&  (browser.indexOf('240x320')>0))\n" +
"{ screenWidth = 240; }\n" +
"screenWidth *= 0.9;\n" +
"var tableWidth = 0;\n" +
"if (defaultWidth < screenWidth)\n" +
"{ tableWidth = defaultWidth; }\n" +
"else\n" +
"{ tableWidth = screenWidth; }\n" +
"// stop hiding JavaScript -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableTag = '<table bgcolor=\"#E0E0C2\" border=0 cellPadding=0 cellSpacing=0 width='+tableWidth+'>\n';\n" +
"document.write(tableTag);\n" +
"// -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableStartTag = '<tr><td>&nbsp;';\n" +
"document.write(tableStartTag);\n" +
"// -->\n" +
"</script>\n" +
"<script language=JavaScript>\n" +
"<!-- \n" +
"var tableTag = '<table bgcolor=\"#E0E0C2\" border=0 cellPadding=1 cellSpacing=1 width='+tableWidth+'>\n';\n" +
"document.write(tableTag);\n" +
"// -->\n" +
"</script>\n" +
"<tr>\n" +
"<td align=center><font size=2 face=\"Verdana\">\n" +
"<b>We are verifying your account.  Please wait.<br></font></td>\n" +
"</tr>\n" +
"<tr>\n" +
"<td align=center><font size=2 face=\"Verdana\">\n" +
"<b><br><br></font></td>\n" +
"<tr><td>&nbsp;</td></tr>\n" +
"<tr><td align=center><font size=2 face=\"Verdana\">Please contact your Network Administrator in case of problems.</font></td></tr>\n" +
"</table></table></body>\n" +
"</html>\n";
	}

	@Override
	public void getHeaders(Map<String, String> headers) {
		headers.put("X-Android-Received-Millis","1385423041389");
		headers.put("Content-Type","text/html");
		headers.put("Content-Length","2155");
		headers.put("Server","GoAhead-Webs");
		headers.put("X-Android-Sent-Millis","1385423041362");
		headers.put("Cache-Control","no-cache");
		headers.put("Pragma","no-cache");
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
		return "http://69.69.112.33:1111/usg/radius?OS=http://www.capitolplazajeffersoncity.com%2Fdining%2Ffountain-court-lounge&prefix=&Counter=1&RLF=";
	}

}
