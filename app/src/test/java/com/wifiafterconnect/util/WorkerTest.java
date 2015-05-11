package com.wifiafterconnect.util;

import android.content.Context;
import android.test.mock.MockContext;
import static org.junit.Assert.*;

import com.wifiafterconnect.BaseTestCase;

import org.junit.Test;

/**
 * Created by brad on 5/12/15.
 *
 * Tests the com.wifiafterconnect.util.Worker class
 */
public class WorkerTest extends BaseTestCase {

    @Test
    public void testWorker() throws Exception {
        final String tag = null;
        Logger l = new Logger(tag);
        Context c = new MockContext();
        Worker w = new Worker(l, c);
        assertEquals(l, getPrivateField(w, "logger"));
        assertEquals(c, getPrivateField(getPrivateField(w, "prefs"), "context"));
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
    public void testIsSaveLogToFile() throws Exception {

    }

    @Test
    public void testSetLogFileName() throws Exception {

    }

    @Test
    public void testToIntent() throws Exception {

    }
}