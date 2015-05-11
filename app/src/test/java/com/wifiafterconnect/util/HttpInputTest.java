package com.wifiafterconnect.util;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by brad on 5/18/15.
 */
public class HttpInputTest {

    @Test
    public void testHttpInputTest() throws Exception {
        HttpInput i = new HttpInput(null);
        assertNull(i.getURL());
        assertEquals("", i.getSource());

        URL url = new URL("http://example.com");
        i = new HttpInput(url);
        assertEquals(url, i.getURL());
        assertEquals("", i.getSource());
    }

    @Test
    public void testGetURL() throws Exception {
        HttpInput i = new HttpInput(null);
        assertNull(i.getURL());

        URL url = new URL("http://example.com");
        i = new HttpInput(url);
        assertEquals(url, i.getURL());
    }

    @Test
    public void testGetSource() throws Exception {
        HttpInput i = new HttpInput(null);
        assertEquals("", i.getSource());

        i.parse("");
        assertEquals("", i.getSource());

        i.parse("source");
        assertEquals("source", i.getSource());
    }

    @Test
    public void testParse() throws Exception {
        HttpInput i = new HttpInput(null);
        assertEquals("", i.getSource());

        assertFalse(i.parse(""));
        assertEquals("", i.getSource());

        assertTrue(i.parse("source"));
        assertEquals("source", i.getSource());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals("", new HttpInput(null).getTitle());
    }

    @Test
    public void testGetURLQueryVar() throws Exception {
        assertNull(new HttpInput(null).getURLQueryVar(null));
        HttpInput i = new HttpInput(new URL("http://example.com"));
        assertNull(i.getURLQueryVar(null));
        assertNull(i.getURLQueryVar(""));
        assertNull(i.getURLQueryVar("query_var"));
        i = new HttpInput(new URL("http://example.com?query_variable=query+val"));
        assertNull(i.getURLQueryVar("query_var"));

        i = new HttpInput(new URL("http://example.com?query_var=query+val"));
        assertEquals("query val", i.getURLQueryVar("query_var"));
    }

    @Test(expected= MalformedURLException.class)
    public void testMakeURL() throws Exception {
        assertNull(new HttpInput(null).makeURL(null));
        assertNull(new HttpInput(null).makeURL(""));

        URL url = new URL("http://example.com/");
        assertEquals(url, new HttpInput(null).makeURL("http://example.com/"));

        HttpInput i = new HttpInput(url);
        assertEquals(new URL("http://example.com/submit.php"), i.makeURL("/submit.php"));
        i = new HttpInput(new URL("http://example.com/sub/dir/"));
        assertEquals(new URL("http://example.com/submit.php"), i.makeURL("/submit.php"));

        // TODO: More error handling needed?
        i = new HttpInput(url);
        assertEquals(new URL("http://example.com/NOT A URL"), i.makeURL("NOT A URL"));
        // Should throw a MalformedURLException
        new HttpInput(new URL("http://example.com")).makeURL("NOT A URL");
    }
}