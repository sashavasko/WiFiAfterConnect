package com.wifiafterconnect.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

public class HHonorsConnTester extends HttpTestConnectionWrapper {
	
	public HHonorsConnTester(Context ctx) {
		super(ctx);
		HttpScriptItem item = null;
		try {
			List<URL> urls = Arrays.asList(
				new URL("http://149.20.37.36/debian/pool/"),
				new URL("http://198.145.20.143/debian/pool/")
			);
			item = new HttpScriptItem(RequestType.GET, urls);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			assert item != null;
			item.setResponseFromAsset("HHonors-DTree-resp1.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		item.addHeader("MIME-Version","1.0");
		item.addHeader("Date","Fri, 14 Feb 2014 00:03:44 GMT");
		item.addHeader("Content-Length","15788");
		item.addHeader("Expires","now");
		item.addHeader("X-Android-Received-Millis","1392336224578");
		item.addHeader("Connection","close");
		item.addHeader("Content-Type","text/html; charset=UTF-8");
		item.addHeader("Server","AOLserver/4.5.1");
		item.addHeader("X-Android-Sent-Millis","1392336224267");

		addItem (item);

		try {
			item = new HttpScriptItem(RequestType.POST, Arrays.asList(
                    new URL("http://nmd.hil-jefdtdt.kan.wayport.net/use_connect.adp")));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			item.setResponseFromAsset("HHonors-DTree-resp2.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		item.addHeader("MIME-Version","1.0");
		item.addHeader("Date","Fri, 14 Feb 2014 00:03:45 GMT");
		item.addHeader("Content-Length","13968");
		item.addHeader("Expires","now");
		item.addHeader("X-Android-Received-Millis","1392336225300");
		item.addHeader("Connection","close");
		item.addHeader("Content-Type","text/html; charset=UTF-8");
		item.addHeader("Server","AOLserver/4.5.1");
		item.addHeader("X-Android-Sent-Millis","1392336225084");
				
		addItem (item);
	}
	
}
