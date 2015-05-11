package com.wifiafterconnect.html;

import java.util.List;

import android.util.Log;

import com.wifiafterconnect.BuildConfig;
import com.wifiafterconnect.html.JavaScript.Token;
import com.wifiafterconnect.JavaScriptTests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class GuestNet1JSTesters extends JavaScriptTests {

	public static final String  VAR_SCRIPT = 
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
			"							*/\n"	;

    public static final String[] CONTROLLER_TYPES = {null, "es", "gn", "mt", "nx", "vp"};
	public static final String[] HOTEL_GROUPS = {null, "tr"};
	public static final String[] HOTEL_BRANDS = {null, "hi","hie","ms","cw","sb","ci","bw","fp","s8"};
	public static final String[] HOTEL_IDS = {null, "20177"};
	public static final String[] USERS = {null, "PC103"};
	public static final String[] PWDS = {null, "yes", "no"};
	public static final String[] REDIRS = {null, "http://www.wiscohotels.com/wiscobaymont/index.php"};
	
	public String formatCustomVars (String contrType, String hotelGroup, String hotelBrand, String hotelId, String usr, String pwd, String redir) {
        return "\t\t" + (contrType == null ? "//" : "  ") +
                " ControllerType = \"" + (contrType == null ? "vp" : contrType) +
                "\";    // Manually Set ControllerType Here\n" +
                "\t\t" + (hotelGroup == null ? "//" : "  ") +
                " HotelGroup = \"" + (hotelGroup == null ? "tr" : hotelGroup) +
                "\";    // Manually Set Hotel Group Here\n" +
		        "\t\t" + (hotelBrand == null ? "//" : "  ") +
                " HotelBrand = \"" + (hotelBrand == null ? "s8" : hotelBrand) +
                "\";    // Manually Set Hotel Brand Here\n" +
                "\t\t" + (hotelId == null ? "//" : "  ") +
                " HotelId = \"" + (hotelId == null ? "20177" : hotelId) +
                "\";    // Manually Set Hotel Code Here\n" +
		        "\t\t" + (usr == null ? "//" : "  ") + " usr = \"" + (usr == null ? "PC103" : usr) +
                "\";    // Manually Set User Here\n" +
		        "\t\t" + (pwd == null ? "//" : "  ") + " pwd = \"" + (pwd == null ? "yes" : pwd) +
                "\";    // Only use \"yes\" or \"no\" to manually indicate that guests need a " +
                "pass code.\n" +
                "\t\t" + (redir == null ? "//" : "  ") + " redir = \"" +
                (redir == null ? "" : redir) + "\";\n";
	}
	
	public static final String  MAIN_SCRIPT = 
			
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
					"						}\n";

	
	public class GuestNet1Tester implements JavaScriptTester {
		
		private String contrType;
		private String hotelGroup;
		private String hotelBrand;
		private String hotelId;
		private String usr;
		private String pwd;
		private String redir;

		public GuestNet1Tester(String contrType, String hotelGroup, String hotelBrand, String hotelId, String usr, String pwd, String redir){
			this.contrType = contrType;
			this.hotelGroup = hotelGroup;
			this.hotelBrand = hotelBrand;
			this.hotelId = hotelId;
			this.usr = usr;
			this.pwd = pwd;
			this.redir = redir;
		}
		
		@Override
		public JavaScript getJS() {
			String script =VAR_SCRIPT + formatCustomVars (contrType, hotelGroup, hotelBrand, hotelId, usr, pwd, redir) + MAIN_SCRIPT;
			Log.d("WifiAfterConnect", script);
			return new JavaScript (script, false /* change to true if test fails */);
		}

		public boolean addAssign (List<Token> tokensClean, String var, String val, boolean bad) {
			if ((!bad && val == null) || (bad && val != null))
				return true;
			tokensClean.add(new JavaScript.Identifier(var));
			tokensClean.add(new JavaScript.Punctuator("="));
			tokensClean.add(new JavaScript.StringLiteral("\""+val+"\""));
			tokensClean.add(new JavaScript.Punctuator(";"));
			return true;
		}

		public boolean addAssignShort (List<Token> tokensClean, String var, String val, boolean bad) {
			if ((!bad && val == null) || (bad && val != null))
				return true;
			tokensClean.add(new JavaScript.Identifier(var));
			tokensClean.add(new JavaScript.Punctuator("="));
			return true;
		}
		
		@Override
		public boolean getGoodTokens(List<Token> tokensClean, int testNo) {
			int t = 0;
			if (testNo == t++) {
				tokensClean.add(new JavaScript.Identifier("cpUrl"));
				tokensClean.add(new JavaScript.Punctuator("="));
				tokensClean.add(new JavaScript.StringLiteral("\"http://login.guestnetinc.com/login.php?\""));
				tokensClean.add(new JavaScript.Punctuator(";"));
				return true;
			}else if (testNo == t++) {
				tokensClean.add(new JavaScript.Identifier("cpUrl"));
				tokensClean.add(new JavaScript.Punctuator("+="));
				tokensClean.add(new JavaScript.StringLiteral("\"pa=\""));
				tokensClean.add(new JavaScript.Punctuator("+"));
				tokensClean.add(new JavaScript.Identifier("pAction"));
				tokensClean.add(new JavaScript.Punctuator(";"));
				return true;
			} else if (testNo == t++) {
				// pAction = \"http://10.0.0.1:8000/&err=\";
				tokensClean.add(new JavaScript.Identifier("pAction"));
				tokensClean.add(new JavaScript.Punctuator("="));
				tokensClean.add(new JavaScript.StringLiteral("\"http://10.0.0.1:8000/&err=\""));
				tokensClean.add(new JavaScript.Punctuator(";"));
				return true;
			} else if (testNo == t++) {
				return addAssign (tokensClean, "ControllerType", contrType, false);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelGroup", hotelGroup, false);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelBrand", hotelBrand, false);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelId", hotelId, false);
			} else if (testNo == t++) {
				return addAssign(tokensClean, "usr", usr, false);
			} else if (testNo == t++) {
				return addAssign(tokensClean, "pwd", pwd, false);
			} else if (testNo == t++) {
				return addAssign(tokensClean, "redir", redir, false);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "ControllerType", contrType, false);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelGroup", hotelGroup, false);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelBrand", hotelBrand, false);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelId", hotelId, false);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "usr", usr, false);
			} else if (testNo == t++) {
                return addAssignShort(tokensClean, "pwd", pwd, false);
            } else {
                return testNo == t && addAssignShort(tokensClean, "redir", redir, false);
            }
        }

		@Override
		public boolean getBadTokens(List<Token> tokensClean, int testNo) {
			int t = 0;
			if (testNo == t++) {
				return addAssign (tokensClean, "ControllerType", contrType, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelGroup", hotelGroup, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelBrand", hotelBrand, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "HotelId", hotelId, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "usr", usr, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "pwd", pwd, true);
			} else if (testNo == t++) {
				return addAssign (tokensClean, "redir", redir, true);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "ControllerType", contrType, true);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelGroup", hotelGroup, true);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelBrand", hotelBrand, true);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "HotelId", hotelId, true);
			} else if (testNo == t++) {
				return addAssignShort (tokensClean, "usr", usr, true);
			} else if (testNo == t++) {
                return addAssignShort(tokensClean, "pwd", pwd, true);
            } else {
                return testNo == t && addAssignShort(tokensClean, "redir", redir, true);
            }
        }

	}

	@Test
	public void testGuestNet1 (){

		for (String ct : CONTROLLER_TYPES){
			for (String hg : HOTEL_GROUPS){
				for (String hb : HOTEL_BRANDS){
					for (String hi : HOTEL_IDS){
						for (String usr : USERS){
							for (String pwd : PWDS){
								for (String redir : REDIRS){
									GuestNet1Tester tester = new GuestNet1Tester (ct, hg, hb, hi, usr, pwd, redir);
									executeTester (tester);
								}
							}
						}
					}
				}
			}
		}
	}

	
}
