package com.wifiafterconnect.pagetesters;

import java.util.Map;

import com.wifiafterconnect.WifiAuthParams;
import com.wifiafterconnect.html.HtmlInput;
import com.wifiafterconnect.PortalHandlers.PortalPageTester;

public class NNUTester implements PortalPageTester {

	@Override
	public String getURL() {
		return "https://wireless.nnu.com/nw4/sites/templatet/free/main";
	}

	@Override
	public String getInput() {
		return 
		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"+
"<html>"+
"<head>"+
"  <title>Wireless Internet Access</title>"+
"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"+
"  <link type=\"text/css\" href=\"styles.css\" rel=\"stylesheet\">"+
"    <script type=\"text/javascript\" src=\"nw4.js\"></script>"+
"  <script type=\"text/javascript\" src=\"AjaxRequest.js\"></script>"+
"  <script type=\"text/javascript\" src=\"events.js\"></script>"+
"  <script type=\"text/javascript\" src=\"json.js\"></script>"+
"  <script type=\"text/javascript\" src=\"overlib.js\"></script>"+
"  <!--[if lt IE 7]>"+
"  <script defer type=\"text/javascript\" src=\"pngfix.js\"></script>"+
"  <![endif]-->"+
"  <script type=\"text/javascript\">addLoadEvent(btn2input);</script>"+
"</head>"+
"<body>"+
"<script type=\"text/javascript\">addLoadEvent(autosubmit);</script>"+
"  <div>"+
"    <a name=\"top\"></a>"+
"    <table width=\"768\" border=\"0\" cellpadding=\"0\""+
"           cellspacing=\"0\""+
"           style=\"margin:auto;background-image:url(getImage/00004431);background-repeat:no-repeat\">"+
"      <tr>"+
"        <td valign=\"top\"> <img alt=\"\" width=\"768\" src=\"getImage/00005153\"> </td>"+
"      </tr>"+
"     <tr>"+
"        <td style=\"padding-top:5px\">"+

"          <table cellspacing=\"0\" cellpadding=\"0\" width=\"768\" border=\"0\">"+
"  <tr>"+
"    <td align=\"center\">"+
"      <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"+
"      <tr>"+
"        <td class=\"menubgcolor\" 	style=\"width:201px\"><img height=\"17\" alt=\"\" src=\"spacer.gif\" width=\"140\"></td>"+
"        <td class=\"menubgcolor\" 	style=\"text-align:center;width:122px\"><a href=\"main\">Home</a></td>"+
"        <td class=\"border\" 		style=\"width:1px\"><img height=\"17\" alt=\"\" src=\"spacer.gif\" width=\"1\"></td>"+
"        <td class=\"menubgcolor\" 	style=\"text-align:center;width:122px\"><a href=\"locationsform\">Locations</a></td>"+
"        <td class=\"border\" 		style=\"width:1px\"><img height=\"17\" alt=\"\" src=\"spacer.gif\" width=\"1\"></td>"+
"        <td class=\"menubgcolor\" 	style=\"text-align:center;width:122px\"><a href=\"support\">Help</a></td>"+
"        <td class=\"border\" 		style=\"width:1px\"><img height=\"17\" alt=\"\" src=\"spacer.gif\" width=\"1\"></td>"+
"        <td class=\"menubgcolor\" 	style=\"text-align:center;width:122px\"><a href=\"whatiswifi\">What is WiFi?</a></td>"+
"        <td class=\"menubgcolor\" 	style=\"width:201px\"><img height=\"17\" alt=\"\" src=\"spacer.gif\" width=\"140\"></td>"+
"      </tr>"+
"      </table>"+
"    </td>"+
"  </tr>"+
"</table>"+
"        </td>"+
"      </tr>"+
"      <tr>"+
"        <td><img src=\"spacer.gif\" width=\"768\" height=\"4\" alt=\"\"></td>"+
"      </tr>"+
"      <tr>"+
"        <td>"+
"<!--"+
"   <?xml version=\"1.0\">"+
"   <carrier_data>"+
"     <page_type>login</page_type>"+
"     <device_type>mikrotik</device_type>"+
"     <login_url>https://wireless.nnu.com/nw4/sites/templatet/free/</login_url>"+
"     <carrier_id>NNU</carrier_id>"+
"     <version>0</version>"+
"     <note>Login Page</note>"+
"   </carrier_data>"+

"<WISPAccessGatewayParam>"+
"  <Redirect>"+
"    <MessageType>100</MessageType>"+
"    <ResponseCode>0</ResponseCode>"+
"    <AccessProcedure>1.0</AccessProcedure>"+
"    <LocationName>NNU</LocationName>"+
"    <AccessLocation>18350</AccessLocation>"+
"    <LoginURL>https://wireless.nnu.com/nw4/sites/templatet/free/</LoginURL>"+
"    <AbortLoginURL>https://rap.nnu.com/logout</AbortLoginURL>"+
"  </Redirect>"+
"</WISPAccessGatewayParam>"+
"-->"+
"  <table width=\"768\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">"+
"    <tr>"+
"      <td rowspan=\"2\"><img src=\"spacer.gif\" width=\"100\" height=\"1\" alt=\"\"></td>"+
"        <td class=\"boxheader\" align=\"center\">FREE WIRELESS ACCESS</td>"+
"      <td><img src=\"spacer.gif\" width=\"14\" height=\"28\" alt=\"\"></td>"+
"        <td class=\"boxheader\" align=\"center\">INFORMATION</td>"+

"      <td rowspan=\"2\"><img src=\"spacer.gif\" width=\"100\" height=\"1\" alt=\"\"></td>"+
"    </tr>"+
"    <tr>"+
"      <td class=\"box\" valign=\"top\">"+
"        <br><br>"+
"          Enter your promo code, accept the terms and conditions, and click Login<br><br>"+
"            <script type=\"text/javascript\">"+
"      function validateForm(form)"+
"      {"+
"        if (form.email.type == 'text')"+
"        {"+
"          if (form.email.value.length == 0)"+
"          {"+
"            alert('Please enter your email address');"+
"            return false;"+
"          }"+
"          var pattern = /^[^@]+@[^@]+.[a-z]{2,}$/i;"+
"          if (form.email.value.search(pattern) == -1)"+
"          {"+
"            alert('Please enter a valid email address');"+
"            return false;"+
"          }"+
"        }"+

"        if (!form.acceptterms.checked)"+
"        {"+
"          alert('You must agree to the terms and conditions');"+
"          return false;"+
"        }"+
"        return true;"+
"      }"+
"    </script>"+

"    <form action=\"loginterms\" method=\"post\" onsubmit=\"return validateForm(this)\">"+
"      <input name=\"logintype\" type=\"hidden\" value=\"promo\">"+
"      <div align=\"center\">"+
"        <input name=\"promo_code\" type=\"text\" size=\"10\"><br><br>"+
"      </div>"+
"      <input name=\"email\" type=\"hidden\" value=\"\"><br>"+
"      <div align=\"center\">"+
"        <table class=\"box\">"+
"          <tr>"+
"            <td>I agree to the<br><a href=\"terms\">Terms and Conditions</a></td>"+
"            <td><input type=\"checkbox\" name=\"acceptterms\"></td>"+
"          </tr>"+
"        </table>"+
"        <br>"+
"        <button type=\"submit\" class=\"button\">Login</button><br><br>"+
"      </div>"+
"    </form>"+

"      </td>"+
"      <td><img src=\"spacer.gif\" width=\"14\" height=\"1\" alt=\"\"></td>"+
"      <td class=\"box\" valign=\"top\">"+
"        <br><br>"+
"          This location provides free wireless service."+
"        <br><br>"+
"        <div align=\"center\">"+
"          <br><br>"+
"        </div>"+
"      </td>"+
"    </tr>"+
"    <tr>"+
"      <td><img src=\"spacer.gif\" width=\"100\" height=\"1\" alt=\"\"></td>"+
"      <td><img src=\"spacer.gif\" width=\"240\" height=\"1\" alt=\"\"></td>"+
"      <td><img src=\"spacer.gif\" width=\"14\"  height=\"1\" alt=\"\"></td>"+
"      <td><img src=\"spacer.gif\" width=\"240\" height=\"1\" alt=\"\"></td>"+
"      <td><img src=\"spacer.gif\" width=\"100\" height=\"1\" alt=\"\"></td>"+
"    </tr>"+
"  </table>"+

"        </td>"+
"      </tr>"+
"      <tr>"+
"        <td class=\"footer\" style=\"height:25px\" align=\"center\" valign=\"middle\">"+
"  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
"  <a href=\"terms\">Terms of Use</a> / <a href=\"privacy\">Privacy Policy</a>"+
"  &nbsp;&nbsp;&nbsp;"+
"  &copy;2013 NetNearU. All Rights Reserved.<img src=\"spacer.gif\" width=\"1\" height=\"25\" alt=\"\" style=\"vertical-align:middle\">"+
"        </td>"+
"      </tr>"+
"      <tr>"+
"        <td style=\"background-color:white;height:100%\">&nbsp;</td>"+
"      </tr>"+
"    </table>"+
"  </div>"+
"</body>"+
"</html>";
	}

	@Override
	public void getHeaders(Map<String, String> headers) {
		headers.put("Date", "Fri, 15 Nov 2013 19:24:39 GMT");
		headers.put("Server", "Zope/(Zope 2.7.3-0, python 2.3.4, linux2) ZServer/1.1");
		headers.put("Content-Length", "6884");
		headers.put("Content-Type", "text/html; charset=utf-8");
	}

	@Override
	public WifiAuthParams getParams() {
		WifiAuthParams params = new WifiAuthParams();
		params.add (new HtmlInput ("promo_code", HtmlInput.TYPE_TEXT, "" ));
		return params;
	}

	@Override
	public String getPostURL() {

		return "https://wireless.nnu.com/nw4/sites/templatet/free/loginterms";
	}

	@Override
	public String getPostData() {
		return "logintype=promo&promo_code=&email=&acceptterms=on";
	}

	@Override
	public String getMetaRefresh() {
		return null;
	}

}
