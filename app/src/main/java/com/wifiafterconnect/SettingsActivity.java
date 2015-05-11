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

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate (Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource ( BuildConfig.DEBUG ? R.xml.preferences_debug : R.xml.preferences);
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
			addPreferencesFromResource ( BuildConfig.DEBUG ? R.xml.preferences_debug : R.xml.preferences);
		}else {
			addPreferencesFragment();
		}
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		if (key.equals(getString(R.string.pref_EnableBackgroundAuth))) {
			WifiBroadcastReceiver.setEnabled(this, sharedPrefs.getBoolean(key,true));
		}
	}
	
}
