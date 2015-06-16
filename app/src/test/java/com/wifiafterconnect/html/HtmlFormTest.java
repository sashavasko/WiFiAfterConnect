package com.wifiafterconnect.html;

import com.wifiafterconnect.BaseTestCase;
import com.wifiafterconnect.BuildConfig;
import com.wifiafterconnect.WifiAuthParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.net.URL;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by brad on 5/11/15.
 *
 * Tests the com.wifiafterconnect.html.HtmlForm class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class HtmlFormTest extends BaseTestCase {
    protected Document document = Jsoup.parse("<form></form>");
    protected HtmlForm empty_form = new HtmlForm(document.getElementsByTag("form").first());

    public HtmlForm formFromHtml(String html) {
        return new HtmlForm(Jsoup.parse(html).getElementsByTag("form").first());
    }

    public boolean isSubmittable(String html) {
        return formFromHtml(html).isSubmittable();
    }

    @Test
    public void testHtmlForm() throws Exception {
        assertEquals("", empty_form.getId());
        assertEquals("", empty_form.getAction());
        assertEquals("", empty_form.getMethod());
        assertEquals("", empty_form.getOnsubmit());
        assertEquals(0, empty_form.getInputs().size());

        String html = "<form id=\"form_id\" action=\".\" method=\"POST\" onsubmit=\"javascript:alert('test')\"><input type=\"email\" name=\"email_name\"></form>";
        HtmlForm f = formFromHtml(html);
        assertEquals("form_id", f.getId());
        assertEquals(".", f.getAction());
        assertEquals("POST", f.getMethod());
        assertEquals("javascript:alert('test')", f.getOnsubmit());
        assertEquals(1, f.getInputs().size());
        assertEquals(HtmlInput.TYPE_EMAIL, f.getInput("email_name").getType());
        assertFalse(f.getInput("email_name").isHidden());

        html = "<form><div class=\"hidedata\"><input type=\"text\" name=\"text_name\"></div></form>";
        f = formFromHtml(html);
        assertEquals(1, f.getInputs().size());
        assertEquals(HtmlInput.TYPE_TEXT, f.getInput("text_name").getType());
        assertTrue(f.getInput("text_name").isHidden());

        html = "<form><div class=\"hidden\"><input type=\"text\" name=\"text_name\"></div></form>";
        f = formFromHtml(html);
        assertEquals(1, f.getInputs().size());
        assertEquals(HtmlInput.TYPE_TEXT, f.getInput("text_name").getType());
        assertTrue(f.getInput("text_name").isHidden());
    }

    @Test
    public void testAddInput() throws Exception {
        assertTrue(empty_form.getInputs().isEmpty());
        empty_form.addInput(null);
        assertTrue(empty_form.getInputs().isEmpty());
        empty_form.addInput(new HtmlInput(null, null, null));
        assertTrue(empty_form.getInputs().isEmpty());
        empty_form.addInput(new HtmlInput("", null, null));
        assertTrue(empty_form.getInputs().isEmpty());
        empty_form.addInput(new HtmlInput("test_name", HtmlInput.TYPE_BUTTON, null));
        assertEquals(1, empty_form.getInputs().size());
        assertEquals("button", empty_form.getInput("test_name").getType());
    }

    @Test
    public void testGetOnsubmit() throws Exception {
        assertEquals("", empty_form.getOnsubmit());
        HtmlForm f = formFromHtml("<form onsubmit=\"javascript:alert('test')\"></form>");
        assertEquals("javascript:alert('test')", f.getOnsubmit());
        f = formFromHtml("<form onsubmit=\"ANYTHING AT ALL\"></form>");
        assertEquals("ANYTHING AT ALL", f.getOnsubmit());
    }

    @Test
    public void testGetMethod() throws Exception {
        assertEquals("", empty_form.getMethod());
        HtmlForm f = formFromHtml("<form method=\"get\"></form>");
        assertEquals("GET", f.getMethod());
        f = formFromHtml("<form method=\"GET\"></form>");
        assertEquals("GET", f.getMethod());
        f = formFromHtml("<form method=\"gEt\"></form>");
        assertEquals("GET", f.getMethod());
        f = formFromHtml("<form method=\"post\"></form>");
        assertEquals("POST", f.getMethod());
        f = formFromHtml("<form method=\"fakemethod\"></form>");
        assertEquals("FAKEMETHOD", f.getMethod());
    }

    @Test
    public void testGetAction() throws Exception {
        assertEquals("", empty_form.getAction());
        empty_form.setAction(".");
        assertEquals(".", empty_form.getAction());
        empty_form.setAction("submit.asp");
        assertEquals("submit.asp", empty_form.getAction());
        empty_form.setAction("/submit/");
        assertEquals("/submit/", empty_form.getAction());
        empty_form.setAction("http://example.com/submit/");
        assertEquals("http://example.com/submit/", empty_form.getAction());
        empty_form.setAction("GOBBLEDIGOOK");
        assertEquals("GOBBLEDIGOOK", empty_form.getAction());
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("", empty_form.getId());
        HtmlForm f = formFromHtml("<form id=\"form_id\"></form>");
        assertEquals("form_id", f.getId());
        f = formFromHtml("<form id=\"ANYTHING AT ALL\"></form>");
        assertEquals("ANYTHING AT ALL", f.getId());
    }

    @Test
    public void testGetInputs() throws Exception {
        assertTrue(empty_form.getInputs().isEmpty());
        empty_form.addInput(new HtmlInput("test_name", "datetime", null));
        assertEquals(1, empty_form.getInputs().size());
        assertEquals("datetime", ((HtmlInput) empty_form.getInputs().toArray()[0]).getType());
    }

    @Test
    public void testHasInput() throws Exception {
        HtmlForm f = formFromHtml("<form><input type=\"text\" name=\"text_name\"></form>");
        assertTrue(f.hasInput("text_name"));
        assertFalse(f.hasInput("fake_name"));
    }

    @Test
    public void testHasInputWithClass() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\" class=\"text_class\"></form>";
        HtmlForm f = formFromHtml(html);
        assertTrue(f.hasInputWithClass("text_class"));
        assertFalse(f.hasInputWithClass("fake_class"));
    }

    @Test
    public void testHasVisibleInput() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\"><div class=\"hidden\"><input type=\"text\" name=\"hidden1\"></div><input type=\"hidden\" name=\"hidden2\"></form>";
        HtmlForm f = formFromHtml(html);
        assertTrue(f.hasVisibleInput("text_name"));
        assertFalse(f.hasVisibleInput("hidden1"));
        assertFalse(f.hasVisibleInput("hidden2"));
        assertFalse(f.hasVisibleInput("fake_name"));
    }

    @Test
    public void testGetInput() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\" class=\"text_class\"></form>";
        HtmlForm f = formFromHtml(html);
        HtmlInput i = (HtmlInput) f.getInputs().toArray()[0];
        assertEquals(i.getType(), f.getInput("text_name").getType());
        assertEquals(i.getName(), f.getInput("text_name").getName());
        assertNull(f.getInput("fake_name"));
    }

    @Test
    public void testGetVisibleInput() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\"><div class=\"hidden\"><input type=\"text\" name=\"hidden1\"></div><input type=\"hidden\" name=\"hidden2\"></form>";
        HtmlForm f = formFromHtml(html);
        assertEquals(f.getInput("text_name"), f.getVisibleInput("text_name"));
        assertNull(f.getVisibleInput("hidden1"));
        assertNull(f.getVisibleInput("hidden2"));
        assertNull(f.getVisibleInput("fake_name"));
    }

    @Test
    public void testGetVisibleInputByType() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\"><input type=\"text\" name=\"text_name2\"><div class=\"hidden\"><input type=\"image\" name=\"image_name\"></div><input type=\"hidden\" name=\"hidden_name\"><input type=\"datetime\" name=\"datetime_name\"></form>";
        HtmlForm f = formFromHtml(html);
        // There are multiple text inputs, we can't predict which will be returned
        String visible_text = f.getVisibleInputByType("text").getName();
        assertTrue(visible_text.equals("text_name") || visible_text.equals("text_name2"));
        assertEquals(f.getInput("datetime_name"), f.getVisibleInputByType("datetime"));
        assertNull(f.getVisibleInputByType("image"));
        assertNull(f.getVisibleInputByType("hidden"));
    }

    @Test
    public void testSetInputValue() throws Exception {
        String html = "<form><input type=\"text\" name=\"text_name\"><input type=\"hidden\" name=\"hidden_name\"></form>";
        HtmlForm f = formFromHtml(html);
        HtmlInput i = f.getInput("text_name");
        assertEquals("", i.getValue());
        assertTrue(f.setInputValue("text_name", "new_value"));
        assertEquals("new_value", f.getInput("text_name").getValue());
        i = f.getInput("hidden_name");
        assertEquals("", i.getValue());
        assertTrue(f.setInputValue("hidden_name", "hidden_value"));
        assertEquals("hidden_value", f.getInput("hidden_name").getValue());
        assertFalse(f.setInputValue("fake_name", "fake_value"));
    }

    @Test
    public void testFormatPostData() throws Exception {
        String html = "<form><input type=\"submit\" name=\"ctl00$ContentPlaceHolder1$submit\" value=\" \"></form>";
        String postData = formFromHtml(html).formatPostData();
        assertEquals("ctl00%24ContentPlaceHolder1%24submit=+", postData);
        html = "<form><input type=\"image\" name=\"image_name\" value=\"image_value\"></form>";
        postData = formFromHtml(html).formatPostData();
        assertEquals("x=1&y=1", postData);
    }

    @Test
    public void testFormatActionURL() throws Exception {
        URL url = new URL("http://example.com");
        assertEquals(url, empty_form.formatActionURL(url));

        HtmlForm f = formFromHtml("<form action=\"http://example.com\"></form>");
        assertEquals(url, f.formatActionURL(url));

        f = formFromHtml("<form action=\"http://example.com/file/\"></form>");
        assertEquals(new URL("http://example.com/file/"), f.formatActionURL(url));

        f = formFromHtml("<form action=\"./file/\"></form>");
        assertEquals(new URL("http://example.com/file/"), f.formatActionURL(url));

        // TODO: Write code to make this test pass!
//        f = formFromHtml("<form action=\"file/\"></form>");
//        assertEquals(new URL("http://example.com/file/"), f.formatActionURL(url));

        // TODO: Write code to make this test pass!
//        f = formFromHtml("<form action=\"file.asp\"></form>");
//        assertEquals(new URL("http://example.com/file.asp"), f.formatActionURL(url));

        f = formFromHtml("<form action=\"../file1/\"></form>");
        URL url2 = new URL("http://example.com/file2/");
        assertEquals(new URL("http://example.com/file2/../file1/"), f.formatActionURL(url2));

        f = formFromHtml("<form action=\"http://example.com/#ref\"></form>");
        assertEquals(new URL("http://example.com/#ref"), f.formatActionURL(url));
    }

    @Test
    public void testFillParams() throws Exception {
        String html = "<form><input type=\"image\" name=\"field_name\" value=\"field_value\"/><input type=\"datetime\" name=\"field_name\" value=\"field_value\"/><input type=\"datetime\" name=\"datetime_name\" value=\"datetime_value\"/><div class=\"hidden\"><input type=\"text\" name=\"text_name\"/></div><input type=\"hidden\" name=\"hidden_name\"/></form>";
        HtmlForm f = formFromHtml(html);
        WifiAuthParams wap = f.fillParams(new WifiAuthParams());
        assertEquals(2, wap.getFields().size());
        assertTrue(wap.hasParam("field_name"));
        String field_type = wap.getField("field_name").getType();
        // there are two inputs with the same name, only one will be added but we don't know which
        assertTrue(field_type.equals("image") || field_type.equals("datetime"));
        assertTrue(wap.hasParam("datetime_name"));
        assertFalse(wap.hasParam("text_name"));
        assertFalse(wap.hasParam("hidden_name"));
    }

    @Test
    public void testIsParamMissing() throws Exception {
        assertFalse(formFromHtml("<form></form>").isParamMissing(new WifiAuthParams(), ""));
        assertFalse(formFromHtml("<form></form>").isParamMissing(null, ""));
        assertFalse(formFromHtml("<form></form>").isParamMissing(null, null));
        HtmlForm f = formFromHtml("<form><input type=\"text\" name=\"visible_field\" value=\"field_value\"/><input type=\"hidden\" name=\"hidden_field\" value=\"field_value\"/></form>");
        assertFalse(f.isParamMissing(null, "hidden_field"));
        assertFalse(f.isParamMissing(new WifiAuthParams(), "hidden_field"));
        assertTrue(f.isParamMissing(null, "visible_field"));
        assertTrue(f.isParamMissing(new WifiAuthParams(), "visible_field"));
        WifiAuthParams wap = f.fillParams(new WifiAuthParams());
        assertFalse(f.isParamMissing(wap, "visible_field"));
    }

    @Test
    public void testFillInputs() throws Exception {
        // Check with null WifiAuthParams
        HtmlForm f = formFromHtml("<form></form>");
        f.fillInputs(null);
        assertEquals(0, f.getInputs().size());

        // Try with empty WifiAuthParams
        f.fillInputs(new WifiAuthParams());
        assertEquals(0, f.getInputs().size());

        // Form with inputs
        f = formFromHtml("<form><input type=\"text\" name=\"text_field\"/></form>");
        assertEquals("", f.getInput("text_field").getValue());
        f.fillInputs(new WifiAuthParams());
        assertEquals("", f.getInput("text_field").getValue());
        WifiAuthParams wap = new WifiAuthParams();
        wap.add(new HtmlInput("datetime_field", "datetime", "datetime_value"));
        f.fillInputs(wap);
        assertEquals("", f.getInput("text_field").getValue());
        wap.add(new HtmlInput("text_field", "text", "text_value"));
        f.fillInputs(wap);
        assertEquals("text_value", f.getInput("text_field").getValue());
    }

    @Test
    public void testIsSubmittable() throws Exception {
        assertFalse(empty_form.isSubmittable());

        String html = "<form><input type=\"submit\"></form>";
        // TODO: is this right? I think nameless submits are quite common
        // Still not submittable because the submit input has no name.
        assertFalse(isSubmittable(html));
        html = "<form><input type=\"submit\" value=\"submit_value\"></form>";
        assertFalse(isSubmittable(html));

        html = "<form><input type=\"submit\" name=\"submit_name\"></form>";
        assertTrue(isSubmittable(html));

        html = "<form><input type=\"checkbox\" name=\"checkbox_name\" value=\"checkbox_value\"><input type=\"submit\" name=\"submit_name\"></form>";
        assertTrue(isSubmittable(html));

        html = "<form><input type=\"checkbox\" name=\"checkbox_name\" value=\"\"><input type=\"submit\" name=\"submit_name\"></form>";
        assertFalse(isSubmittable(html));

        html = "<form><input type=\"checkbox\" name=\"checkbox_name\"><input type=\"submit\" name=\"submit_name\"></form>";
        assertFalse(isSubmittable(html));

        html = "<form><input type=\"text\" name=\"text_name\"><input type=\"submit\" name=\"submit_name\"></form>";
        assertFalse(isSubmittable(html));

        html = "<form><input type=\"text\" name=\"text_name\" value=\"text_value\"><input type=\"submit\" name=\"submit_name\"></form>";
        assertTrue(isSubmittable(html));

        html = "<form><input type=\"text\" name=\"text_name\" value=\"text_value\"></form>";
        assertFalse(isSubmittable(html));
    }

    @Test
    public void testSetAction() throws Exception {
        assertEquals("", empty_form.getAction());
        empty_form.setAction("http://example.com");
        assertEquals("http://example.com", empty_form.getAction());
        empty_form.setAction(null);
        assertEquals("", empty_form.getAction());
        empty_form.setAction("http://example.com/file#ref");
        assertEquals("http://example.com/file#ref", empty_form.getAction());
        // TODO: Evaluate whether action should be set to "" here
        empty_form.setAction("MALFORMED_URL");
        assertEquals("MALFORMED_URL", empty_form.getAction());
    }
}