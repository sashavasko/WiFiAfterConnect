package com.wifiafterconnect;

import com.wifiafterconnect.html.HtmlInput;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by brad on 5/16/15.
 *
 * Tests the org.wifiafterconnect.WifiAuthParams class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class WifiAuthParamsTest extends BaseTestCase {

    @Test
    public void testWifiAuthParams() throws Exception {
        assertEquals("username", WifiAuthParams.USERNAME);
        assertEquals("password", WifiAuthParams.PASSWORD);
        assertEquals("email", WifiAuthParams.EMAIL);
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals("DEFAULT", wap.authAction.name());
        assertEquals("DEFAULT", wap.wifiAction.name());
        assertFalse(wap.savePassword);
        assertEquals(0, ((HashMap) getPrivateField(wap, "fields")).size());
    }

    @Test
    public void testGetFields() throws Exception {
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals(0, wap.getFields().size());

        HtmlInput i = new HtmlInput("field_name", null, null);
        wap.add(i);
        assertEquals(1, wap.getFields().size());
        assertTrue(wap.getFields().contains(i));
    }

    @Test
    public void testAdd() throws Exception {
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals(0, wap.getFields().size());

        // Check that invalid inputs don't get added
        wap.add(new HtmlInput(null, null, null));
        assertEquals(0, wap.getFields().size());

        HtmlInput i = new HtmlInput("field_name", null, null);
        wap.add(i);
        assertEquals(1, wap.getFields().size());
        assertTrue(wap.getFields().contains(i));
    }

    @Test
    public void testHasParam() throws Exception {
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals(0, wap.getFields().size());
        assertFalse(wap.hasParam("non_existant_field"));

        wap.add(new HtmlInput("empty_field", null, null));
        assertEquals(1, wap.getFields().size());
        assertFalse(wap.hasParam("empty_field"));

        wap.add(new HtmlInput("field_name", null, "value"));
        assertEquals(2, wap.getFields().size());
        assertTrue(wap.hasParam("field_name"));
    }

    @Test
    public void testIsSupportedParamType() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_PASSWORD);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_EMAIL);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_NUMBER);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_TEL);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_DATE);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_DATETIME);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_DATETIME_LOCAL);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType(HtmlInput.TYPE_TIME);
        assertTrue(WifiAuthParams.isSupportedParamType(i));
        i.setType("FAKE_TYPE");
        assertFalse(WifiAuthParams.isSupportedParamType(i));
    }

    @Test
    public void testGetField() throws Exception {
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals(0, wap.getFields().size());
        assertNull(wap.getField("non_existant_field"));

        HtmlInput i = new HtmlInput("empty_field", null, null);
        wap.add(i);
        assertEquals(1, wap.getFields().size());
        assertEquals(i, wap.getField("empty_field"));

        i = new HtmlInput("good_field", "datetime", "value");
        wap.add(i);
        assertEquals(2, wap.getFields().size());
        assertEquals(i, wap.getField("good_field"));
    }

    @Test
    public void testGetFieldByType() throws Exception {
        WifiAuthParams wap = new WifiAuthParams();
        HtmlInput i = new HtmlInput("field_name", "text", null);
        wap.add(i);
        // TODO: Shouldn't this pass? I'm guessing this method isn't being used anywhere
        //       since it doesn't behave as one might expect
        // assertEquals(i, wap.getFieldByType("text"));
        assertNull(wap.getFieldByType("password"));
        i = new HtmlInput("password", "password", null);
        wap.add(i);
        assertEquals(i, wap.getFieldByType("password"));

    }

    @Test
    public void testToString() throws Exception {
        // Try a default object
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals("<WifiAuthParams authAction=\"DEFAULT\" wifiAction=\"DEFAULT\" savePassword=\"false\"></WifiAuthParams>",
                wap.toString());

        // Try with an input
        wap.add(new HtmlInput("field_name", null, null));
        assertEquals("<WifiAuthParams authAction=\"DEFAULT\" wifiAction=\"DEFAULT\" savePassword=\"false\"><input name=\"field_name\" type=\"text\" value=\"\" class=\"\" onClick=\"\" form=\"\" checked=\"\"></WifiAuthParams>",
                wap.toString());
    }

    @Test
    public void testToString1() throws Exception {
        // Try passing null
        assertEquals("(null)", WifiAuthParams.toString(null));

        // Try a default object
        WifiAuthParams wap = new WifiAuthParams();
        assertEquals("<WifiAuthParams authAction=\"DEFAULT\" wifiAction=\"DEFAULT\" savePassword=\"false\"></WifiAuthParams>",
                WifiAuthParams.toString(wap));

        // Try with an input
        wap.add(new HtmlInput("field_name", null, null));
        assertEquals("<WifiAuthParams authAction=\"DEFAULT\" wifiAction=\"DEFAULT\" savePassword=\"false\"><input name=\"field_name\" type=\"text\" value=\"\" class=\"\" onClick=\"\" form=\"\" checked=\"\"></WifiAuthParams>",
                WifiAuthParams.toString(wap));
    }
}