package com.wifiafterconnect.html;

import android.text.InputType;

import com.wifiafterconnect.BaseTestCase;

import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

/**
 * Created by brad on 5/10/15.
 *
 * For testing the com.wifiafterconnect.html.HtmlInput class
 */
public class HtmlInputTest extends BaseTestCase {

    @Test
    public void testHtmlInput() throws Exception {
        // Check defaults
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getName());
        assertEquals("text", i.getType());
        assertEquals("", i.getValue());
        assertFalse(i.isClass("FAKE_CLASS"));
        assertTrue(i.isClass(""));
        assertEquals("", i.getOnClick());
        assertEquals("", i.getFormId());
        assertFalse(i.isHidden());
        assertEquals("<input name=\"\" type=\"text\" value=\"\" class=\"\" onClick=\"\" form=\"\" checked=\"\">", i.toString());

        // Test non-defaults
        i = new HtmlInput("image_name", HtmlInput.TYPE_IMAGE, "FAKE_VALUE");
        assertEquals("image_name", i.getName());
        assertEquals("image", i.getType());
        assertEquals("FAKE_VALUE", i.getValue());
        assertEquals("<input name=\"image_name\" type=\"image\" value=\"FAKE_VALUE\" class=\"\" onClick=\"\" form=\"\" checked=\"\">", i.toString());

        // Test jsoup element constructor
        Document doc = Jsoup.parse("<input checked='true' type='checkbox'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("", i.getName());
        assertEquals("checkbox", i.getType());
        assertEquals("", i.getValue());
        assertTrue(i.isClass(""));
        assertFalse(i.isClass("fake_class"));
        assertEquals("", i.getOnClick());
        assertEquals("", i.getFormId());
        assertFalse(i.isHidden());
        assertEquals("<input name=\"\" type=\"checkbox\" value=\"\" class=\"\" onClick=\"\" form=\"\" checked=\"true\">", i.toString());

        doc = Jsoup.parse("<input onClick=\"javascript:alert('test')\" form=\"hidden_form\" class=\"hidden_class\" type=\"hidden\" name=\"hidden_name\" value=\"hidden_value\">");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("hidden_name", i.getName());
        assertEquals("hidden", i.getType());
        assertEquals("hidden_value", i.getValue());
        assertTrue(i.isClass("hidden_class"));
        assertFalse(i.isClass("fake_class"));
        assertEquals("javascript:alert('test')", i.getOnClick());
        assertEquals("hidden_form", i.getFormId());
        assertTrue(i.isHidden());
        String hidden_html = "<input name=\"hidden_name\" type=\"hidden\" value=\"hidden_value\" class=\"hidden_class\" onClick=\"javascript:alert('test')\" form=\"hidden_form\" checked=\"\">";
        assertEquals(hidden_html, i.toString());

        // Test other constructor
        HtmlInput hidden = new HtmlInput(i);
        assertEquals(hidden.getName(), i.getName());
        assertEquals(hidden.getType(), i.getType());
        assertEquals(hidden.getValue(), i.getValue());
        assertTrue(hidden.isClass("hidden_class"));
        assertFalse(hidden.isClass("fake_class"));
        assertEquals(hidden.getOnClick(), i.getOnClick());
        assertEquals(hidden.getFormId(), i.getFormId());
        assertEquals(hidden.isHidden(), i.isHidden());
        assertEquals(hidden_html, hidden.toString());
    }

    @Test
    public void testSetType() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("text", i.getType());
        i.setType(HtmlInput.TYPE_DATETIME);
        assertEquals("datetime", i.getType());
        i.setType("");
        assertEquals("text", i.getType());
        i.setType("FAKE_TYPE");
        assertEquals("FAKE_TYPE", i.getType());
        i.setType(null);
        assertEquals("text", i.getType());
    }

    @Test
    public void testIsHidden() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertFalse(i.isHidden());
        i.setType(HtmlInput.TYPE_RADIO);
        assertFalse(i.isHidden());
        i.setType(HtmlInput.TYPE_HIDDEN);
        assertTrue(i.isHidden());
        i.setType("HIDDEN");
        assertTrue(i.isHidden());
        Document doc = Jsoup.parse("<input type='hidden' name='hidden_name' value='hidden_value'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertTrue(i.isHidden());
        doc = Jsoup.parse("<input type='HIDDEN' name='hidden_name' value='hidden_value'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertTrue(i.isHidden());
        doc = Jsoup.parse("<input type='text' name='hidden_name' value='hidden_value'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertFalse(i.isHidden());
        doc = Jsoup.parse("<input type='text' name='hidden_name' value='hidden_value'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), true);
        assertTrue(i.isHidden());
    }

    @Test
    public void testIsValid() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertFalse(i.isValid());
        i = new HtmlInput("", null, null);
        assertFalse(i.isValid());
        i = new HtmlInput(null, "submit", null);
        // TODO: Is this right? I think it is quite common for submit inputs to be nameless
        assertFalse(i.isValid());
        i = new HtmlInput("VALID_NAME", null, null);
        assertTrue(i.isValid());
        i = new HtmlInput("ANY NON-EMPTY NAME IS VALID", null, null);
        assertTrue(i.isValid());
        i = new HtmlInput("INVALID @*#&%!@)(*&@#&%", null, null);
        assertTrue(i.isValid());
    }

    @Test
    public void testGetName() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getName());
        i = new HtmlInput("real_name", null, null);
        assertEquals("real_name", i.getName());
    }

    @Test
    public void testGetType() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("text", i.getType());
        i = new HtmlInput(null, HtmlInput.TYPE_BUTTON, null);
        assertEquals("button", i.getType());
        i = new HtmlInput(null, "FAKE_TYPE", null);
        assertEquals("FAKE_TYPE", i.getType());
    }

    @Test
    public void testGetValue() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getValue());
        i = new HtmlInput(null, null, "real_value");
        assertEquals("real_value", i.getValue());
    }

    @Test
    public void testMatchType() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertTrue(i.matchType("text"));
        assertTrue(i.matchType("TEXT"));
        i = new HtmlInput(null, HtmlInput.TYPE_BUTTON, null);
        assertTrue(i.matchType("button"));
        assertTrue(i.matchType("BUTTON"));
        i = new HtmlInput(null, "FAKE_TYPE", null);
        assertTrue(i.matchType("FAKE_TYPE"));
        assertTrue(i.matchType("fake_type"));
    }

    @Test
    public void testGetFormId() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getFormId());
        Document doc = Jsoup.parse("<input form=\"form_id\">");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("form_id", i.getFormId());
    }

    @Test
    public void testSetValue() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getValue());
        i.setValue("test_value");
        assertEquals("test_value", i.getValue());
        i.setValue(null);
        assertEquals("", i.getValue());
    }

    @Test
    public void testGetOnClick() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals("", i.getOnClick());
        Document doc = Jsoup.parse("<input onClick=\"javascript:alert('test')\">");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("javascript:alert('test')", i.getOnClick());
    }

    @Test
    public void testIsClass() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertTrue(i.isClass(""));
        assertFalse(i.isClass("fake_class"));
        Document doc = Jsoup.parse("<input class=\"class_name\">");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertFalse(i.isClass(""));
        assertTrue(i.isClass("class_name"));
    }

    @Test
    public void testFormatPostData() throws Exception {
        HtmlInput i = new HtmlInput("ctl00$ContentPlaceHolder1$submit", "submit", " ");
        StringBuilder postData = new StringBuilder();
        i.formatPostData(postData);
        assertEquals("&ctl00%24ContentPlaceHolder1%24submit=+", postData.toString());
        i = new HtmlInput("image_name", HtmlInput.TYPE_IMAGE, "image_value");
        postData = new StringBuilder();
        i.formatPostData(postData);
        assertEquals("&x=1&y=1", postData.toString());
    }

    @Test
    public void testGetAndroidInputType() throws Exception {
        HtmlInput i = new HtmlInput(null, null, null);
        assertEquals(i.getAndroidInputType(), InputType.TYPE_CLASS_TEXT);
        i.setType(HtmlInput.TYPE_PASSWORD);
        assertEquals(i.getAndroidInputType(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        i.setType(HtmlInput.TYPE_EMAIL);
        assertEquals(i.getAndroidInputType(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        i.setType(HtmlInput.TYPE_NUMBER);
        assertEquals(i.getAndroidInputType(), InputType.TYPE_CLASS_NUMBER);
        i.setType(HtmlInput.TYPE_TEL);
        assertEquals(i.getAndroidInputType(), InputType.TYPE_CLASS_NUMBER);
        i.setType(HtmlInput.TYPE_DATE);
        assertEquals(i.getAndroidInputType(),
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        i.setType(HtmlInput.TYPE_DATETIME);
        assertEquals(i.getAndroidInputType(), InputType.TYPE_CLASS_DATETIME);
        i.setType(HtmlInput.TYPE_DATETIME_LOCAL);
        assertEquals(i.getAndroidInputType(), InputType.TYPE_CLASS_DATETIME);
        i.setType(HtmlInput.TYPE_TIME);
        assertEquals(i.getAndroidInputType(),
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        i.setType("FAKE_TYPE");
        assertEquals(i.getAndroidInputType(), 0);
    }

    @Test
    public void testToString() throws Exception {
        Document doc = Jsoup.parse("<input onClick=\"javascript:alert('test')\" form=\"hidden_form\" class=\"hidden_class\" type=\"hidden\" name=\"hidden_name\" value=\"hidden_value\">");
        HtmlInput i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("<input name=\"hidden_name\" type=\"hidden\" value=\"hidden_value\" class=\"hidden_class\" onClick=\"javascript:alert('test')\" form=\"hidden_form\" checked=\"\">", i.toString());


        doc = Jsoup.parse("<input checked='true' type='checkbox'>");
        i = new HtmlInput(doc.getElementsByTag("input").first(), false);
        assertEquals("<input name=\"\" type=\"checkbox\" value=\"\" class=\"\" onClick=\"\" form=\"\" checked=\"true\">", i.toString());
    }
}