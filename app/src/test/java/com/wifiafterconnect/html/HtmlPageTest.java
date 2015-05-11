package com.wifiafterconnect.html;

import com.wifiafterconnect.BaseTestCase;
import com.wifiafterconnect.BuildConfig;
import com.wifiafterconnect.util.HttpInput;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by brad on 5/18/15.
 *
 * Tests the com.wifiafterconnect.html.HtmlPage class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class HtmlPageTest extends BaseTestCase {

    @Test(expected= MalformedURLException.class)
    public void testMetaRefresh() throws Exception {
        String url = "http://example.com";
        HtmlPage p = new HtmlPage(new URL(url));
        assertTrue(p.parse(
                "<head><meta http-equiv=\"refresh\" content=\"10;url=" + url + "\"></head>"));
        assertEquals(10, p.getMetaRefresh().getTimeout());
        assertEquals(url, p.getMetaRefresh().getURLString());
        assertEquals(new URL(url), p.getMetaRefresh().getURL());

        p.parse("<head><meta http-equiv=\"refresh\" content=\"url=BADURL\"></head>");
        assertEquals(0, p.getMetaRefresh().getTimeout());
        assertEquals("BADURL", p.getMetaRefresh().getURLString());
        // Throws a MalformedURLException
        p.getMetaRefresh().getURL();
    }

    @Test
    public void testHtmlPage() throws Exception {
        HtmlPage p = new HtmlPage(null);
        assertNull(p.getURL());
        assertEquals(0, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(0, ((List) getPrivateField(p, "forms")).size());
        assertEquals(0, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertEquals("", p.getOnLoad());
        assertEquals("", p.getTitle());
        assertNull(p.getWISPr());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals(0, ((Map) getPrivateField(p, "httpEquivMetas")).size());

        URL url = new URL("http://example.com");
        p = new HtmlPage(url);
        assertEquals(url, p.getURL());
        assertEquals(0, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(0, ((List) getPrivateField(p, "forms")).size());
        assertEquals(0, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertEquals("", p.getOnLoad());
        assertEquals("", p.getTitle());
        assertNull(p.getWISPr());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals(0, ((Map) getPrivateField(p, "httpEquivMetas")).size());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals("", new HtmlPage(null).getTitle());
        // TODO: Set the title and verify that getTitle returns the new value
    }

    @Test
    public void testIsJavaScript() throws Exception {
        HtmlPage p = new HtmlPage(null);
        Element div = Jsoup.parse("<div></div>").getElementsByTag("div").first();
        Element template = Jsoup.parse("<script type=\"text/template\"></script>")
                .getElementsByTag("script").first();
        Element type_js = Jsoup.parse("<script type=\"text/javascript\"></script>")
                .getElementsByTag("script").first();
        Element lang_js = Jsoup.parse("<script language=\"javascript\"></script>")
                .getElementsByTag("script").first();
        assertFalse(p.isJavaScript(div));
        assertFalse(p.isJavaScript(template));
        assertTrue(p.isJavaScript(type_js));
        assertTrue(p.isJavaScript(lang_js));
    }

    @Test
    public void testParse() throws Exception {
        HtmlPage p = new HtmlPage(new URL("http://example.com"));

        assertFalse(p.parse(""));

        // With title and form
        assertTrue(p.parse("<html>" +
                "<title>\nTitle\n</title>" +
                "<body><form></form></body>" +
                "</html>"));
        assertEquals(0, ((Map) getPrivateField(p, "httpEquivMetas")).size());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        // TODO: Make this test pass!
        // assertEquals("Title", p.getTitle());
        assertEquals("", p.getOnLoad());
        assertEquals(1, p.forms().size());
        assertEquals(0, ((HtmlForm) p.forms().toArray()[0]).getInputs().size());
        assertEquals(0, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(0, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertNull(p.getWISPr());

        // With onLoad and form with id and inputs
        p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<body onload=\"javascript:alert('test')\">" +
                "<form id=\"form\">" +
                "<input type=\"text\" name=\"username\" value=\"fake_username\"/>" +
                "<input type=\"password\" name=\"password\" value=\"fake_password\"/>" +
                "</form>" +
                "</body>"));
        assertEquals(0, ((Map) getPrivateField(p, "httpEquivMetas")).size());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals("javascript:alert('test')", p.getOnLoad());
        assertEquals(1, p.forms().size());
        assertEquals(1, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals("fake_username", p.getForm("form").getInput("username").getValue());
        assertEquals("fake_password", p.getForm("form").getInput("password").getValue());
        assertEquals(0, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertNull(p.getWISPr());

        // With metas and scripts
        p = new HtmlPage(new URL("http://example.com"));
        String viewCont = "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no";
        assertTrue(p.parse("<head>" +
                "<meta http-equiv=\"refresh\" content=\"30\">" +
                "<meta name=\"robots\" content=\"noindex\">" +
                "<meta name=\"viewport\" content=\"" + viewCont + "\">" +
                "<script type=\"text/javascript\">alert('head test');</script>" +
                "<script type=\"text/template\">Head template</script>" +
                "</head>" +
                "<body>" +
                "<script type=\"text/javascript\">alert('body test');</script>" +
                "<script type=\"text/template\">body template</script>" +
                "</body>"));
        List bodyJavaScripts = (List) getPrivateField(p, "bodyJavaScripts");
        assertEquals(1, ((Map) getPrivateField(p, "httpEquivMetas")).size());
        assertTrue(p.hasMetaRefresh());
        assertEquals(0, p.getMetaRefresh().getTimeout());
        assertEquals("", p.getMetaRefresh().getURLString());
        assertEquals(2, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals("noindex", p.getMeta("robots"));
        assertEquals(viewCont, p.getMeta("viewport"));
        assertEquals("", p.getOnLoad());
        assertEquals(0, p.forms().size());
        assertEquals(0, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(1, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals("alert ( 'head test' ) ; ", p.getHeadJavaScript("alert").getClean());
        assertEquals(1, bodyJavaScripts.size());
        assertEquals("alert ( 'body test' ) ; ", ((JavaScript) bodyJavaScripts.get(0)).getClean());
        assertNull(p.getWISPr());

        // With javascript http-equiv refresh and input form attrs
        p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<head>" +
                "<script type=\"text/javascript\">" +
                "var pAction = 'paction';" +
                "var ControllerType = 'controllertype';" +
                "var HotelGroup = 'hotelgroup';" +
                "var HotelBrand = 'hotelbrand';" +
                "var HotelId = 'hotelid';" +
                "var usr = 'usr';" +
                "var pwd = 'pwd';" +
                "var cpUrl = 'http://example.com/submit.php?';" +
                "document.write('<meta http-equiv=\"REFRESH\" content=\"0;url=' + cpUrl + '\">');" +
                "</script>" +
                "</head>" +
                "<input type=\"text\" name=\"text1\" form=\"form20\">" +
                "<input type=\"text\" name=\"text2\" form=\"form1\">" +
                "<form id=\"form1\">" +
                "<input type=\"text\" name=\"text3\" form=\"form1\">" +
                "</form>"));
        assertEquals(1, ((Map) getPrivateField(p, "httpEquivMetas")).size());
        assertTrue(p.hasMetaRefresh());
        assertEquals(0, p.getMetaRefresh().getTimeout());
        assertEquals("http://example.com/submit.php?pa=paction&ct=controllertype&hg=hotelgroup&hb=hotelbrand&id=hotelid&usr=usr&pwd=pwd",
                p.getMetaRefresh().getURLString());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals("", p.getOnLoad());
        assertEquals(1, p.forms().size());
        assertEquals(1, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(1, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertNull(p.getWISPr());

        // Test wISPr page
        // TODO: wISPr parser should be able to handle extra white space at the beginning of the
        // comment, right?
        p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<HTML>" +
                "<BODY><H2>Browser error!</H2>Browser does not support redirects!</BODY>\n" +
                "<!--" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<WISPAccessGatewayParam\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:noNamespaceSchemaLocation=\"http://www.wballiance.net/wispr_2_0.xsd\">\n" +
                "<Redirect>\n" +
                "<MessageType>100</MessageType>\n" +
                "<ResponseCode>0</ResponseCode>\n" +
                "<VersionHigh>2.0</VersionHigh>\n" +
                "<VersionLow>1.0</VersionLow>\n" +
                "<AccessProcedure>1.0</AccessProcedure>\n" +
                "<AccessLocation>CDATA[[isocc=,cc=,ac=,network=Coova,]]</AccessLocation>\n" +
                "<LocationName>CDATA[[My_HotSpot]]</LocationName>\n" +
                "<LoginURL>" +
                "https://example.com/login.php?res=wispr&amp;uamip=10.1.0.1&amp;uamport=3990" +
                "</LoginURL>\n" +
                "<AbortLoginURL>http://10.1.0.1:3990/abort</AbortLoginURL>\n" +
                "<EAPMsg>AQEABQE=</EAPMsg>\n" +
                "</Redirect>\n" +
                "</WISPAccessGatewayParam>\n" +
                "-->\n" +
                "</HTML>"));
        assertEquals(0, ((Map) getPrivateField(p, "httpEquivMetas")).size());
        assertEquals(0, ((Map) getPrivateField(p, "namedMetas")).size());
        assertEquals("", p.getOnLoad());
        assertEquals(0, p.forms().size());
        assertEquals(0, ((Map) getPrivateField(p, "namedForms")).size());
        assertEquals(0, ((List) getPrivateField(p, "headJavaScripts")).size());
        assertEquals(0, ((List) getPrivateField(p, "bodyJavaScripts")).size());
        assertEquals("https://example.com/login.php?res=wispr&uamip=10.1.0.1&uamport=3990",
                getPrivateField(p.getWISPr(), "loginURL"));
    }

    @Test
    public void testGetHeadJavaScript() throws Exception {
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertNull(p.getHeadJavaScript(null));
        assertNull(p.getHeadJavaScript("alert"));
        assertTrue(p.parse("<head>" +
                "<script type=\"text/javascript\">alert('head test');</script>" +
                "<script type=\"text/template\">Head template</script>" +
                "<script type=\"text/javascript\">alert('head test2');</script>" +
                "</head>"));
        assertEquals(2, ((List) getPrivateField(p, "headJavaScripts")).size());
        String js = p.getHeadJavaScript("alert").getClean();
        assertTrue(js.equals("alert ( 'head test' ) ; ") || js.equals("alert ( 'head test2' ) ; "));
        assertNull(p.getHeadJavaScript("Head template"));
        assertEquals("alert ( 'head test' ) ; ",
                p.getHeadJavaScript("alert('head test'").getClean());
        assertEquals("alert ( 'head test2' ) ; ",
                p.getHeadJavaScript("alert('head test2'").getClean());
    }

    @Test
    public void testGetOnLoad() throws Exception {
        assertEquals("", new HtmlPage(null).getTitle());
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        p.parse("<html><body onload=\"javascript:alert('test')\"></body></html>");
        assertEquals("javascript:alert('test')", p.getOnLoad());
    }

    @Test
    public void testGetWISPr() throws Exception {
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertNull(p.getWISPr());
        assertTrue(p.parse("<HTML>" +
                "<BODY><H2>Browser error!</H2>Browser does not support redirects!</BODY>\n" +
                "<!--" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<WISPAccessGatewayParam\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:noNamespaceSchemaLocation=\"http://www.wballiance.net/wispr_2_0.xsd\">\n" +
                "<Redirect>\n" +
                "<MessageType>100</MessageType>\n" +
                "<ResponseCode>0</ResponseCode>\n" +
                "<VersionHigh>2.0</VersionHigh>\n" +
                "<VersionLow>1.0</VersionLow>\n" +
                "<AccessProcedure>1.0</AccessProcedure>\n" +
                "<AccessLocation>CDATA[[isocc=,cc=,ac=,network=Coova,]]</AccessLocation>\n" +
                "<LocationName>CDATA[[My_HotSpot]]</LocationName>\n" +
                "<LoginURL>" +
                "https://example.com/login.php?res=wispr&amp;uamip=10.1.0.1&amp;uamport=3990" +
                "</LoginURL>\n" +
                "<AbortLoginURL>http://10.1.0.1:3990/abort</AbortLoginURL>\n" +
                "<EAPMsg>AQEABQE=</EAPMsg>\n" +
                "</Redirect>\n" +
                "</WISPAccessGatewayParam>\n" +
                "-->\n" +
                "</HTML>"));
        assertEquals("https://example.com/login.php?res=wispr&uamip=10.1.0.1&uamport=3990",
                getPrivateField(p.getWISPr(), "loginURL"));
    }

    @Test
    public void testGetForm() throws Exception {
        // The empty signature
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertNull(p.getForm());
        assertTrue(p.parse("<form></form>"));
        assertEquals(0, p.getForm().getInputs().size());
    }

    @Test
    public void testGetForm1() throws Exception {
        // The int signature
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertNull(p.getForm(0));
        assertNull(p.getForm(1));
        assertTrue(p.parse(
                "<form id=\"form\"><input type=\"text\" name=\"text_field\"></form><form></form>"));
        HtmlForm f = p.getForm(0);
        if(f.getId().equals("form")) {
            assertEquals(1, f.getInputs().size());
            assertEquals(0, p.getForm(1).getInputs().size());
        } else {
            assertEquals(0, f.getInputs().size());
            assertEquals(1, p.getForm(1).getInputs().size());
        }
    }

    @Test
    public void testGetForm2() throws Exception {
        // The static (HtmlPage,int) signature
        assertNull(HtmlPage.getForm(null, 0));
        assertNull(HtmlPage.getForm(new HttpInput(null), 0));
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertNull(HtmlPage.getForm(p, 0));
        assertNull(HtmlPage.getForm(p, 1));
        assertTrue(p.parse(
                "<form id=\"form\"><input type=\"text\" name=\"text_field\"></form><form></form>"));
        HtmlForm f = HtmlPage.getForm(p, 0);
        if(f.getId().equals("form")) {
            assertEquals(1, f.getInputs().size());
            assertEquals(0, HtmlPage.getForm(p, 1).getInputs().size());
        } else {
            assertEquals(0, f.getInputs().size());
            assertEquals(1, HtmlPage.getForm(p, 0).getInputs().size());
        }
    }

    @Test
    public void testGetForm3() throws Exception {
        // The string signature
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        String form_id = null;
        assertNull(p.getForm(form_id));
        assertNull(p.getForm("form"));
        assertTrue(p.parse(
                "<form id=\"form\"><input type=\"text\" name=\"text_field\"></form><form></form>"));
        assertEquals(1, p.getForm("form").getInputs().size());
    }

    @Test
    public void testGetForm4() throws Exception {
        // The static (HtmlPage,string) signature
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        String form_id = null;
        assertNull(HtmlPage.getForm(p, form_id));
        assertNull(HtmlPage.getForm(p, "form"));
        assertTrue(p.parse(
                "<form id=\"form\"><input type=\"text\" name=\"text_field\"></form><form></form>"));
        assertEquals(1, HtmlPage.getForm(p, "form").getInputs().size());
    }

    @Test
    public void testGetForm5() throws Exception {
        // The static HtmlPage signature
        HtmlPage p = null;
        assertNull(HtmlPage.getForm(p));
        assertNull(HtmlPage.getForm(new HttpInput(null)));
        p = new HtmlPage(new URL("http://example.com"));
        assertNull(HtmlPage.getForm(p));
        assertTrue(p.parse("<form></form>"));
        assertEquals(0, HtmlPage.getForm(p).getInputs().size());
    }

    @Test
    public void testForms() throws Exception {
        assertEquals(0, new HtmlPage(null).forms().size());
        HtmlPage p = new HtmlPage(null);
        assertTrue(p.parse("<form></form><form></form>"));
        assertEquals(2, p.forms().size());
    }

    @Test
    public void testHasMetaRefresh() throws Exception {
        String url = "http://example.com";
        HtmlPage p = new HtmlPage(new URL(url));
        assertFalse(p.hasMetaRefresh());
        assertTrue(p.parse("<head>" +
                "<meta http-equiv=\"expires\" content=\"mon, 27 sep 2015 14:30:00 GMT\">" +
                "</head>"));
        assertFalse(p.hasMetaRefresh());
        p = new HtmlPage(new URL(url));
        assertTrue(p.parse("<head>" +
                "<meta http-equiv=\"expires\" content=\"mon, 27 sep 2015 14:30:00 GMT\">" +
                "<meta http-equiv=\"refresh\" content=\"10;url=" + url + "\">" +
                "</head>"));
        assertTrue(p.hasMetaRefresh());
    }

    @Test
    public void testGetMetaRefresh() throws Exception {
        String url = "http://example.com";
        HtmlPage p = new HtmlPage(new URL(url));
        assertNull(p.getMetaRefresh());
        assertTrue(p.parse("<head>" +
                "<meta http-equiv=\"expires\" content=\"mon, 27 sep 2015 14:30:00 GMT\">" +
                "</head>"));
        assertNull(p.getMetaRefresh());
        p = new HtmlPage(new URL(url));
        assertTrue(p.parse("<head>" +
                "<meta http-equiv=\"expires\" content=\"mon, 27 sep 2015 14:30:00 GMT\">" +
                "<meta http-equiv=\"refresh\" content=\"10;url=" + url + "\">" +
                "</head>"));
        assertEquals(url, p.getMetaRefresh().getURLString());
        assertEquals(10, p.getMetaRefresh().getTimeout());
    }

    @Test
    public void testGetMeta() throws Exception {
        String url = "http://example.com";
        String viewCont = "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no";
        HtmlPage p = new HtmlPage(new URL(url));
        assertEquals("", p.getMeta("robots"));
        assertTrue(p.parse("<head>" +
                "<meta name=\"viewport\" content=\"" + viewCont + "\">" +
                "</head>"));
        assertEquals("", p.getMeta("robots"));
        p = new HtmlPage(new URL(url));
        assertTrue(p.parse("<head>" +
                "<meta name=\"viewport\" content=\"" + viewCont + "\">" +
                "<meta http-equiv=\"refresh\" content=\"30\">" +
                "<meta name=\"robots\" content=\"noindex\">" +
                "</head>"));
        assertEquals(viewCont, p.getMeta("viewport"));
        assertEquals("noindex", p.getMeta("robots"));
    }

    @Test
    public void testGetDocumentReadyFunc() throws Exception {
        // TODO: implement JavaScript.getDocumentReadyFunc! (if needed)
        HtmlPage p = new HtmlPage(null);
        assertNull(p.getDocumentReadyFunc());
        assertTrue(p.parse("<head>" +
                "<script type=\"text/javascript\">document.onreadystatechange = function () {" +
                "alert('head test');" +
                "};</script>" +
                "<script type=\"text/template\">Head template</script>" +
                "<script type=\"text/javascript\">alert('head test2');</script>" +
                "</head>"));
        String documentReadyFunc = p.getDocumentReadyFunc();
        //assertEquals("function () {alert('head test');}, documentReadyFunc);
    }

    @Test
    public void testHasFormWithInputType() throws Exception {
        assertFalse(new HtmlPage(null).hasFormWithInputType("checkbox"));
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<input type=\"text\" name=\"field1\" form=\"form20\">" +
                "<input type=\"datetime\" name=\"field2\" form=\"form1\">" +
                "<form id=\"form1\">" +
                "<input type=\"image\" name=\"field3\">" +
                "<input type=\"hidden\" name=\"field4\">" +
                "</form>"));
        assertFalse(p.hasFormWithInputType("text"));
        assertTrue(p.hasFormWithInputType("datetime"));
        assertTrue(p.hasFormWithInputType("image"));
        assertFalse(p.hasFormWithInputType("hidden"));
    }

    @Test
    public void testHasSubmittableForm() throws Exception {
        assertFalse(new HtmlPage(null).hasSubmittableForm());
        HtmlPage p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<form></form>"));
        assertFalse(p.hasSubmittableForm());
        p = new HtmlPage(new URL("http://example.com"));
        assertTrue(p.parse("<form>" +
                "<input type=\"checkbox\" name=\"checkbox_name\" value=\"checkbox_value\">" +
                "<input type=\"submit\" name=\"submit_name\">" +
                "</form>"));
        assertTrue(p.hasSubmittableForm());
    }
}