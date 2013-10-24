package com.wifiafterconnect.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.wifiafterconnect.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Preferences {

	private Context context;
	
	public Preferences (Context context){
		this.context = context;
	}

	public boolean getSaveLogToFile () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_saveLogFileEnable", false);
	}
	
	public File getSaveLogLocation () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPrefs.getBoolean ("pref_saveLogDir", true) 
				&&	Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return context.getExternalFilesDir(null);
		}
		return context.getFilesDir();
	}
	
	public boolean getAutoDisableWifi () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_AutoDisableWifi", true);
	}
	
	public boolean getReenableWifiQuiet () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_ReenableWifiQuiet", true);
	}

	public URL getURLToCheckHttp  () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		URL url = null;
		try {
			url = new URL(sharedPrefs.getString ("pref_URLToCheckHTTP", Constants.URL_TO_CHECK_HTTP));
		} catch (MalformedURLException e) {
			try {
				url = new URL(Constants.URL_TO_CHECK_HTTP);
			} catch (MalformedURLException e1) {} // should never happen
		}
		return url;
	} 

	public URL getURLToCheckHttps () {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		URL url = null;
		try {
			url = new URL(sharedPrefs.getString ("pref_URLToCheckHTTPS", Constants.URL_TO_CHECK_HTTPS));
		} catch (MalformedURLException e) {
			try {
				url = new URL(Constants.URL_TO_CHECK_HTTPS);
			} catch (MalformedURLException e1) {} // should never happen
		}
		return url;
	} 

}
