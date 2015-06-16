package com.wifiafterconnect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.LargeTest;

import com.wifiafterconnect.http.HttpConnectionFactory;
import com.wifiafterconnect.http.HHonorsConnTester;
import com.wifiafterconnect.pagetesters.CiscoSwitchUrlTester;
import com.wifiafterconnect.pagetesters.ColubrisTester;
import com.wifiafterconnect.pagetesters.GuestNet1Tester;
import com.wifiafterconnect.pagetesters.MikrotikTester;
import com.wifiafterconnect.pagetesters.NNUTester;
import com.wifiafterconnect.pagetesters.WifiSoftTester;
import com.wifiafterconnect.util.Logger;
import com.wifiafterconnect.util.Worker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class PortalHandlers extends InstrumentationTestCase {

    public interface PortalPageTester {
		String getURL();
		String getInput();
		void getHeaders(Map<String, String> headers);
		WifiAuthParams getParams();
		String getPostURL();
		String getPostData();
		String getMetaRefresh();
	}

    @Rule
    public final ActivityRule<WifiAuthenticatorActivity> activityRule =
            new ActivityRule<WifiAuthenticatorActivity>(WifiAuthenticatorActivity.class);

    private WifiAuthenticatorActivity wifiAuthActivity;

    public PortalHandlers() {
        super();

        //Instrumentation instrumentation = activityRule.fetchInstrumentation();
        String targetPackage = "com.wifiafterconnect";
        Intent i = activityRule.getLaunchIntent(targetPackage, WifiAuthenticatorActivity.class);
        i.putExtra(WifiAuthenticator.OPTION_URL, "http://example.com");
        i.putExtra(WifiAuthenticator.OPTION_PAGE, "<html></html>");
        wifiAuthActivity = activityRule.get(i);
    }

	@Test
    public void testPreconditions() {
		assertNotNull(wifiAuthActivity);
		assertNotNull(InstrumentationRegistry.getInstrumentation());
	}

	public void executeTester (PortalPageTester tester) {
		Worker base = new Worker (new Logger("WiFiAfterConnect.test"), new MockContext());
		URL url = null;
		try {
			url = new URL (tester.getURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		Map<String,String> headers = new HashMap<>();
		tester.getHeaders(headers);
		ParsedHttpInput parsedPage = new ParsedHttpInput (base, url, tester.getInput(), headers);
		assertNotNull ("ParsedPage is null", parsedPage);
		
		for (String hk : headers.keySet()) {
				assertEquals ("Header is incorrect", headers.get(hk), parsedPage.getHttpHeader(hk));	
		}
		
		WifiAuthParams wantParams = tester.getParams();
		if (wantParams == null)
			wantParams = new WifiAuthParams();
		WifiAuthParams haveParams = parsedPage.addMissingParams(null);
		assertEquals ("Params are incorrect", WifiAuthParams.toString(wantParams), WifiAuthParams.toString(haveParams));
		if (tester.getPostURL() != null)
			assertEquals ("Post URL does not match", tester.getPostURL(), parsedPage.getFormPostURL().toString());
		else {
			URL refreshURL = parsedPage.makeRefreshURL(null);
			assertEquals ("meta Refresh URL does not match", tester.getMetaRefresh(), refreshURL == null ? "null" : refreshURL.toString());
		}
		assertEquals ("Post Data does not match", tester.getPostData(), parsedPage.buildPostData(wantParams));
	}

    @Test
	public void testCiscoSwitchUrl (){
		executeTester (new CiscoSwitchUrlTester());
	}

    @Test
	public void testColubris (){
		executeTester (new ColubrisTester());
	}

    @Test
	public void testMikrotik (){
		executeTester (new MikrotikTester());
	}

    @Test
	public void testNNU (){
		executeTester (new NNUTester());
	}

    @Test
	public void testGuestNet1 (){
		executeTester (new GuestNet1Tester());
	}

    @Test
	public void testWifiSoft (){
		executeTester (new WifiSoftTester());
	}

    @Test
	public void testHHonors (){
		//executeTester (new WifiSoftTester());
		Context ctx = InstrumentationRegistry.getInstrumentation().getContext();
		URLRedirectChecker checker = new URLRedirectChecker (Constants.TAG, ctx);
		HttpConnectionFactory.INSTANCE.setConnectionInstance(new HHonorsConnTester(ctx));
		checker.setSaveLogFile (null);
		checker.checkHttpConnection ();
	}

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
