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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver {

	public void onWifiConnectivityChange(Context context, boolean connected) {
		if (connected)
			CheckRedirectService.startService(context);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(Constants.TAG, "WifiBroadcastReceiver: connectivity change");
        }
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) 
			&& intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, 0) == ConnectivityManager.TYPE_WIFI){
			boolean connected = !intent.getBooleanExtra (ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            Log.d(Constants.TAG, "WifiBroadcastReceiver: connectivity change for Wifi, connected=" + connected);
            onWifiConnectivityChange(context, connected);

		} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo wifiNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (wifiNetworkInfo != null) {
                Log.d(Constants.TAG, "WifiBroadcastReceiver: Current Wifi state is " +
                        wifiNetworkInfo.getDetailedState() + "/" + wifiNetworkInfo.getState());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                        wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING ||
                        wifiNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    onWifiConnectivityChange(context, true);
                }
            }
        }
	}
	
	public static void setEnabled (Context context, boolean enable) {
		if (context == null)
			return;
		PackageManager pm = context.getPackageManager();
		if (pm == null)
			return;
		ComponentName component = new ComponentName (context, WifiBroadcastReceiver.class);
		int status = pm.getComponentEnabledSetting(component);
		int statusNew = enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		if (status != statusNew)
			pm.setComponentEnabledSetting(component,  statusNew, PackageManager.DONT_KILL_APP);
	}
	
	public static boolean isEnabled (Context context) {
		int status = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
		if (context != null) {
			PackageManager pm = context.getPackageManager();
			if (pm != null) {
				ComponentName component = new ComponentName (context, WifiBroadcastReceiver.class);
				status = pm.getComponentEnabledSetting(component);
			}
		}
		return (status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
	}
}
