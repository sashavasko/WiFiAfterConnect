package com.wifiafterconnect.util;

import android.content.Intent;

import com.wifiafterconnect.BaseTestCase;
import com.wifiafterconnect.BuildConfig;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.Assert.*;

/**
 * Created by brad on 5/12/15.
 *
 * Tests the com.wifiafterconnect.util.Logger class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class LoggerTest extends BaseTestCase {
    private String loggerTag;
    private String loggerFile;
    private final String null_tag = null;

    @Before
    public void setUp() throws Exception {
        loggerTag = (String) getPrivateField(new Logger(null_tag), "OPTION_LOGGER_TAG");
        loggerFile = (String) getPrivateField(new Logger(null_tag), "OPTION_LOGGER_FILE");
    }

    @Test
    public void testPreconditions() {
        assertEquals("PARAM_LOGGER_TAG", loggerTag);
        assertEquals("PARAM_LOGGER_FILE", loggerFile);
    }

    @Test
    public void testLogger() throws Exception {
        Logger l = new Logger(null_tag);
        assertEquals("", getPrivateField(l, "tag"));
        assertNull(getPrivateField(l, "file"));
        assertNull(getPrivateField(l, "logFile"));
        final String tag2 = "test_tag";
        l = new Logger(tag2);
        assertEquals("test_tag", getPrivateField(l, "tag"));
        assertNull(getPrivateField(l, "file"));
        assertNull(getPrivateField(l, "logFile"));
        final String tag3 = "012345678901234567890123456789012";
        l = new Logger(tag3);
        // Tags greater than 32 characters get clipped
        assertEquals("01234567890123456789012345678901", getPrivateField(l, "tag"));
        assertNull(getPrivateField(l, "file"));
        assertNull(getPrivateField(l, "logFile"));

        // Logger with an intent
        Intent i = new Intent();
        l = new Logger(i);
        assertNull(getPrivateField(l, "tag"));
        assertNull(getPrivateField(l, "file"));
        assertNull(getPrivateField(l, "logFile"));

        i.putExtra(loggerTag, tag2);
        i.putExtra(loggerFile, "test.log");
        l = new Logger(i);
        assertEquals("test_tag", getPrivateField(l, "tag"));
        File f = (File) getPrivateField(l, "file");
        assertTrue(f.exists());
        assertEquals("test.log", f.getPath());
        Object o = getPrivateField(l, "logFile");
        assertNotNull(o);
        assertTrue(o instanceof PrintWriter);
    }

    @Test
    public void testGetTag() throws Exception {
        final String tag = null;
        Logger l = new Logger(tag);
        assertEquals("", invokePrivateMethod(l, "getTag"));
        final String tag2 = "test_tag";
        l = new Logger(tag2);
        assertEquals("test_tag", invokePrivateMethod(l, "getTag"));
        final String tag3 = "012345678901234567890123456789012";
        l = new Logger(tag3);
        // Tags greater than 32 characters get clipped
        assertEquals("01234567890123456789012345678901", invokePrivateMethod(l, "getTag"));
    }

    @Test
    public void testSetLogFile() throws Exception {

    }

    @Test
    public void testException() throws Exception {

    }

    @Test
    public void testDebug() throws Exception {

    }

    @Test
    public void testError() throws Exception {

    }

    @Test
    public void testToIntent() throws Exception {
        final String tag = "fake_tag";
        Logger l = new Logger(tag);

        Intent i = new Intent();
        l.toIntent(i);
        assertEquals("fake_tag", i.getStringExtra(loggerTag));
    }
}