package com.wifiafterconnect.http;

import android.content.Context;
import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;

import com.wifiafterconnect.util.Worker;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author sasha
 */
public class HttpTestConnectionWrapper extends HttpConnectionWrapper {

    public enum RequestType{
        POST, GET;
    };

    private Context context;

    public HttpTestConnectionWrapper(Context ctx)
    {
        super();
        context = ctx;
    }

    public class HttpScriptItem extends InstrumentationTestCase {

        private RequestType reqType;
        private List<URL> urls;
        private String postDataString;
        protected String response;
        protected Map<String,String> responseHeaders = new HashMap<String,String>();

        public void doPost(Worker context, String postDataString,
                           String cookies, String referer) {
            assertTrue (reqType == RequestType.POST);
            assertTrue("Request Url", urls.contains(getUrl()));
            assertEquals("Post data", this.postDataString, postDataString);
        }

        public void doGet(Worker context, String referer) {
            assertTrue(reqType == RequestType.GET);
            assertTrue("Request Url", urls.contains(getUrl()));
        }

        public HttpScriptItem(RequestType reqType, List<URL> urls) {
            this.reqType = reqType;
            this.urls = urls;
        }

        public void addHeader (String name, String value) {
            responseHeaders.put (name, value);
        }

        public void setResponse (String response) {
            this.response = response;
        }

        public void setResponse (InputStream ims) throws IOException {
            StringBuilder total = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(ims));
            char[] buffer = new char[4096];
            int bytesIn;
            while ((bytesIn = r.read(buffer)) >= 0) {
                if (bytesIn > 0) {
                    total.append(buffer);
                }
            }
            response = total.toString();
        }

        public void setResponseFromAsset (String assetName) throws IOException {
            //Context ctx = getInstrumentation().getContext();
            AssetManager assets = context.getAssets();
            InputStream ims = assets.open(assetName);
            setResponse (ims);
        }
    }


    private List<HttpScriptItem> script = new ArrayList<HttpScriptItem>();
    private int currentStep = 0;

    private void setFrom(HttpScriptItem item) {
        headers = item.responseHeaders;
        data = item.response;
    }

    /* (non-Javadoc)
     * @see com.wifiafterconnect.http.HttpConnectionWrapper#post(com.wifiafterconnect.util.Worker, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean post(Worker context, String postDataString, String cookies,
                        String referer) {
        if (script.size() <= currentStep)
            return false;
        HttpScriptItem item = script.get(currentStep++);
        item.doPost(context, postDataString, cookies, referer);
        setFrom(item);
        return true;
    }

    /* (non-Javadoc)
     * @see com.wifiafterconnect.http.HttpConnectionWrapper#get(com.wifiafterconnect.util.Worker, java.lang.String)
     */
    @Override
    public boolean get(Worker context, String referer) {
        if (script.size() <= currentStep)
            return false;
        HttpScriptItem item = script.get(currentStep++);
        item.doGet(context, referer);
        setFrom(item);
        return true;
    }

    public void addItem(HttpScriptItem item){
        script.add(item);

    }

    public void clearScript() {
        script.clear();
    }

}