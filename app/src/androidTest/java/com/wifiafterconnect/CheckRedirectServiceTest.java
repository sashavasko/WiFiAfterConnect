package com.wifiafterconnect;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;

import org.junit.Test;

/**
 * Created by brad on 5/27/15.
 *
 * Test the CheckRedirectService service
 */
public class CheckRedirectServiceTest extends ServiceTestCase<CheckRedirectService> {

    public CheckRedirectServiceTest() {
        super(CheckRedirectService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @Test
    public void testStartable() {
        Intent startIntent = new Intent();
        CheckRedirectService.startService(getContext());
    }

    /**
     * Test binding to service
     */
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), CheckRedirectService.class);
        IBinder service = bindService(startIntent);
    }
}