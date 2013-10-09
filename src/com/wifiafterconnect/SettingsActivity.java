/*
 * Copyright (C) 2013 Sasha Vasko <sasha at aftercode dot net> 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wifiafterconnect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate (Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource (R.xml.preferences);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) public void addPreferencesFragment() {
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new SettingsFragment())
		.commit();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource (R.xml.preferences);
		}else {
			addPreferencesFragment();
		}
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	public static boolean getSaveLogToFile (Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_saveLogFileEnable", false);
	}
	
	public static File getSaveLogLocation (Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (sharedPrefs.getBoolean ("pref_saveLogDir", true) 
				&&	Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return context.getExternalFilesDir(null);
		}
		return context.getFilesDir();
	}
	
	public static boolean getAutoDisableWifi (Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_AutoDisableWifi", true);
	}
	
	public static boolean getReenableWifiQuiet (Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean ("pref_ReenableWifiQuiet", true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		if (key.equals(getString(R.string.pref_EnableBackgroundAuth))) {
			WifiBroadcastReceiver.setEnabled(this, sharedPrefs.getBoolean(key,true));
		}
	}
	
	public static URL getUrlToCheckHttp  (Context context) {
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

	public static URL getUrlToCheckHttps  (Context context) {
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
