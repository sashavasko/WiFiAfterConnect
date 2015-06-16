package com.wifiafterconnect;

import android.content.Intent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by brad on 5/15/15.
 *
 * Tests for com.wifiafterconnect.WifiAuthenticatorActivity class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class WifiAuthenticatorActivityTest extends BaseTestCase {

    @Test
    public void testOnAuthenticateClick() throws Exception {

    }

    @Test
    public void testIsAlwaysDoThat() throws Exception {

    }

    @Test
    public void testPerformAction() throws Exception {

    }

    @Test
    public void testOnCancelClick() throws Exception {

    }

    @Test
    public void testOnOpenBrowserClick() throws Exception {

    }

    @Test
    public void testOnCreate() throws Exception {
        Intent i = new Intent();
        i.putExtra(WifiAuthenticator.OPTION_URL, "http://example.com");
        i.putExtra(WifiAuthenticator.OPTION_PAGE, "<html></html>");
        WifiAuthenticatorActivity a = Robolectric.buildActivity(WifiAuthenticatorActivity.class)
                .withIntent(i).create().resume().visible().get();
        // TODO: Somehow verify the layout is correct
        assertFalse(a.isAlwaysDoThat());
        assertEquals(new URL("http://example.com"), getPrivateField(a, "url"));
        WifiAuthenticator wifiAuth = (WifiAuthenticator) getPrivateField(a, "wifiAuth");
        assertEquals("example.com", getPrivateField(wifiAuth, "authHost"));
        ParsedHttpInput p = (ParsedHttpInput) getPrivateField(a, "parsedPage");
        assertEquals("<html></html>", p.getHtml());
        WifiAuthParams wap = (WifiAuthParams) getPrivateField(a, "authParams");
        assertEquals(0, wap.getFields().size());
        assertEquals(WifiAuthenticator.AuthAction.DEFAULT, wap.authAction);
        TableLayout t = (TableLayout) getPrivateField(a, "fieldsTable");
        assertNotNull(t);
        assertEquals(0, t.getChildCount());
        assertEquals(0, ((ArrayList) getPrivateField(a, "edits")).size());
        assertNull(getPrivateField(a, "checkSavePassword"));
        Button b = (Button) getPrivateField(a, "buttonAuthenticate");
        assertNotNull(b);
        CheckBox c = (CheckBox) getPrivateField(a, "checkAlwaysDoThat");
        assertNotNull(c);

        // TODO: Test onCreate with a page with a form and inputs
    }

    @Test
    public void testSaveAction() throws Exception {

    }
}