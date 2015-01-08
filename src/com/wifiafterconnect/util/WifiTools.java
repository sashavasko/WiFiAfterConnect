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

package com.wifiafterconnect.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

public class WifiTools {

	private static WifiTools instance = null;
	
	protected WifiManager wifiMan = null;
	protected Context context;

	private WifiTools (Context context) {
		this.context = context;
		if (context != null)
			wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}

	public static WifiTools getInstance(Context ctx) {
		if (instance == null)
			instance = new WifiTools(ctx.getApplicationContext());
		return instance;
	}
	
	
	public enum Action { 
		DEFAULT, ENABLE, DISABLE, KEEP;
		
		public static Action parse(final String string) {
			if (string != null) {
				for (Action a : Action.values())
					if (string.equalsIgnoreCase(a.toString())) 
						return a;
			}
			return DEFAULT;
		}
		
		public boolean perform (Context context) {
			if (this == DISABLE)
				WifiTools.disableWifi (context);
			else if (this == ENABLE)
				WifiTools.enableWifi (context);
			
			return (this != DEFAULT); 
		}
	}
	
	public void disableWifi() {
		setWifiEnabled(false);
	}

	public void enableWifi() {
		setWifiEnabled(true);
	}

	public void setWifiEnabled(boolean enable) {
		if (wifiMan != null) {
			try {
				wifiMan.setWifiEnabled(enable);
			}catch (SecurityException e) {
				// Stupid Galaxy Tab requires CHANGE_NETWORK_STATE permission, 
				// god knows what other devices might have - better add the handler
				// see http://kmansoft.com/2011/12/05/wifi-manager-2-1-6/
				e.printStackTrace();
			}
		}
	}
	
	public boolean isWifiEnabled() {
		return wifiMan != null ? wifiMan.isWifiEnabled() : false;
	}
	
	public static void disableWifi(Context context) {
		setWifiEnabled(context, false);
	}

	public static void enableWifi(Context context) {
		setWifiEnabled(context, true);
	}

	public static void setWifiEnabled(Context context, boolean enable) {
		if (context != null) {
			WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			if (wifiMan != null) {
				try {
					wifiMan.setWifiEnabled(enable);
				}catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean isWifiEnabled(Context context) {
		if (context != null) {
			WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			if (wifiMan != null)
				return wifiMan.isWifiEnabled();
		}
		return false;
	}
	
	public static String getSSID(Context context) {
		if (context != null) {
			WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			if (wifiMan != null) {
				WifiInfo info = wifiMan.getConnectionInfo();
				if (info != null) {
					return info.getSSID();
				}
			}
		}
		return null;
	}

    public static void preferWifi(Context context) {
        if (context != null) {
            ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMan != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Network wifiNetwork = getFirstWifiNetwork(context);
                    if (wifiNetwork != null) {
                        ConnectivityManager.setProcessDefaultNetwork(wifiNetwork);
                    }
                } else {
                    //Try in API 17-20 to prefer wifi
                    connMan.setNetworkPreference(ConnectivityManager.TYPE_WIFI);
                }
            }
        }
    }

    public static boolean isWifiAvailable(Context context) {
        NetworkInfo wifiNetworkInfo = getFirstWifiNetworkInfo(context);
        //For some reason, ConnectivityManager shows wifi as disconnected even when connected to
        //captive portal (but the WifiManager intent showed us the true state).
        return wifiNetworkInfo != null && wifiNetworkInfo.isAvailable();
    }

	public boolean isWifiConnected (){
		return isWifiAvailable(context);
	}

    public static NetworkInfo getFirstWifiNetworkInfo(Context context) {
        if (context != null) {
            ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMan != null){
                for (NetworkInfo networkInfo : connMan.getAllNetworkInfo()) {
                    //If there is a WIFI network that isn't completely disconnected, then try it out
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return networkInfo;
                    }
                }
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Network getFirstWifiNetwork(Context context) {
        if (context != null) {
            ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMan != null){
                for (Network network : connMan.getAllNetworks()) {
                    NetworkInfo networkInfo = connMan.getNetworkInfo(network);
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        return network;
                    }
                }
            }
        }
        return null;
    }

}
