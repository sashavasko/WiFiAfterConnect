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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import com.wifiafterconnect.URLRedirectChecker.AuthorizationType;
import com.wifiafterconnect.util.Logger;
import com.wifiafterconnect.util.WifiTools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

public class WifiAuthenticator {

	public enum AuthAction { 
		DEFAULT, BROWSER, IGNORE;
	
		public static AuthAction parse(final String string) {
			if (string != null) {
				for (AuthAction a : AuthAction.values())
					if (string.equalsIgnoreCase (a.toString())) 
						return a;
			}
			return DEFAULT;
		}
		
	}
	
	private Context context;
	private Logger logger;
	
	public WifiAuthenticator (Context context, Logger logger) {
		this.context = context;
		this.logger = logger;
	}
	
	private WifiAuthDatabase wifiDb = null;
	protected boolean checkPrepareDB() {
		if (context != null) {
			if (wifiDb == null)
				wifiDb = WifiAuthDatabase.getInstance(context);
		}
		return (wifiDb != null);
	}
	
	public WifiAuthParams getStoredAuthParams(String authHost) {
		return checkPrepareDB()? wifiDb.getAuthParams (authHost): null;
	}
	public void storeAuthAction (final String authHost, final AuthAction action) {
		if (checkPrepareDB())
			wifiDb.storeAuthAction (authHost, action);
	}

	public AuthAction getAuthAction (final String authHost) {
		return checkPrepareDB()? wifiDb.getAuthAction (authHost): null;
	}

	public void storeWifiAction (final String authHost, final WifiTools.Action action) {
		if (checkPrepareDB())
			wifiDb.storeWifiAction (authHost, action);
	}

	public WifiTools.Action getWifiAction (final String authHost) {
		return checkPrepareDB()? wifiDb.getWifiAction (authHost): null;
	}
	
	public static final String OPTION_URL = "PARAM_URL";
	public static final String OPTION_PAGE = "PARAM_PAGE";
	
	protected void notifyWifiDisabled(String hostname) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
		.setSmallIcon (R.drawable.wifiac_small)//(android.R.drawable.presence_offline)
		.setContentTitle(context.getString(R.string.notif_wifi_disabled_title))
		.setContentText(hostname + " - " + context.getString(R.string.notif_wifi_disabled_text));
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		if (SettingsActivity.getReenableWifiQuiet(context))
			resultIntent.setAction(context.getString(R.string.action_reenable_wifi));
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		builder.setContentIntent(resultPendingIntent);
		
		Notification n = builder.build();
		logger.debug("posting notification that wifi was disabled (" + n.toString() + ")");

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, n);
	}
	/*
	 * Android starting with 4.0 has watchdog that checks for walled garden if it detects on it 
	 * posts a notification which may mislead user as we will take care of it.
	 * There is no API/config setting to turn it off, so we'll try this little hack.
	 * Probably not going to work as I suspect notifications are per-package, but we can try:
	 * 
	 * Alternative is to change settings in /data/data/com.android.providers.settings/databases/settings.db
	 * 
	 *  Either:
	 *  In Android 4.0 : 			secure.wifi_watchdog_show_disabled_network_popup
	 *  In Android 4.1 : 			secure.wifi_watchdog_walled_garden_test_enabled
	 *  In Android 4.2 and 4.3 	:	global.captive_portal_detection_enabled
	 * ( can use command from su shell: settings put global captive_portal_detection_enabled 0 )
	 *   
	 *  the code responsible was android/net/wifi/WifiWatchdogStateMachine.java
	 *  Android 4.1 and WifiWatchdogStateMachine duties has changed into monitoring connection for
	 *  packet loss (setting global.wifi_watchdog_on). Still does some walled Garden check in 
	 *  WifiWatchdogStateMachine.WalledGardenCheckState.
	 *
	 *  Android 4.2 and later : functionality moved into android.net.CaptivePortalTracker
	 * 
	 * 
	 */
	
	private static final String CAPTIVE_PORTAL_TRACKER_NOTIFICATION_ID = "CaptivePortal.Notification"; // Android 4.2 and later
	private static final String WALLED_GARDEN_NOTIFICATION_ID = "WifiWatchdog.walledgarden"; // Android 4.1
	private static final String WATCHDOG_NOTIFICATION_ID = "Android.System.WifiWatchdog"; // Android 4.0
	public void cancelWatchdogNotification() {
		if (context != null) {
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(WATCHDOG_NOTIFICATION_ID, 1);
			nm.cancel(WALLED_GARDEN_NOTIFICATION_ID, 1);
			nm.cancel(CAPTIVE_PORTAL_TRACKER_NOTIFICATION_ID, 1);
		}
	}
	
	protected void requestUserParams (URL url, ParsedHttpInput parsedPage) {
		
		logger.debug("Need user input for authentication credentials.");
		
		if (context == null) {
			logger.error ("Context is not set - cannot start WifiAuthenticationActivity");
			return;
		}
		// Need to check that screen is not off:
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			/** Screen is Off
			 * if Disable Wifi is enabled :  
			 * 	1) Disable wifi (if configured).
			 * 	2) Post notification with intent to re-enable wifi.
			 * otherwise : 
			 *  setup Broadcast receiver waiting for SCREEN_ON event, 
			 *  which will restart the service on wake-up if wifi is still connected. 
			 *  Don't just want to pop the Activity and let it sit there, 
			 *  as Wifi may get disconnected and device moved to another location meanwhile 
			 **/
			boolean disableWifiOnLock = SettingsActivity.getAutoDisableWifi(context);
			
			logger.debug("Screen is off and disableWifiOnLock is " + disableWifiOnLock);
			
			if (disableWifiOnLock) {
				WifiTools.disableWifi(context);
				notifyWifiDisabled(url.getHost());
			}else {
				ScreenOnReceiver.register(context);
				// don't want to receive repeat notifications - will re-enable when screen comes on
				WifiBroadcastReceiver.setEnabled(context, false);				
			}
		}else {
			logger.debug("Screen is on - Starting new acivity.");
			/**
			 *  Screen is On - so proceeding displaying activity asking for credentials.
			 */
			Intent intent = new Intent(context, WifiAuthenticatorActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra (OPTION_URL, url.toString());
			intent.putExtra (OPTION_PAGE, parsedPage.getHtml());
			logger.toIntent(intent);
			logger.debug("Starting acivity for intent:"+intent.toString());
			context.startActivity (intent);
		}
	}

	public ParsedHttpInput postForm (URL url, ParsedHttpInput parsedPage, WifiAuthParams authParams) {

		String postDataString = parsedPage.buildPostData(authParams);
		logger.debug ("Data to post:["+postDataString+"]");
		if (postDataString.isEmpty()){
			logger.error("Failed to compile data for authentication POST");
			return null;
		}

		HttpURLConnection conn = null;
		try {
			URL postUrl = new URL (url.getProtocol() + "://" + url.getAuthority() + url.getPath());
			conn = (HttpURLConnection) postUrl.openConnection();
			conn.setConnectTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setReadTimeout(Constants.SOCKET_TIMEOUT_MS);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			
			DataOutputStream output = new DataOutputStream (conn.getOutputStream());
			output.writeBytes(postDataString);
			output.flush();
			output.close();
			logger.debug("Data posted, checking result ...");
			return ParsedHttpInput.receive(logger, conn);
		}catch ( ProtocolException e)
		{
			logger.exception (e);
			return null;
		} catch (IOException e) {
			logger.exception (e);
			return null;
		}finally {
			conn.disconnect();
		}
	}
	
	public boolean attemptAuthorization (URL url, ParsedHttpInput parsedPage, WifiAuthParams authParams) {
		
		if (parsedPage.submitOnLoad()) {
			logger.debug("Handling onLoad submit for [" + url + "]");
			parsedPage = postForm (url, parsedPage, null);
		}
		
		logger.debug("Attempting authorization at [" + url + "]");
		if (!parsedPage.isKnownCaptivePortal()) {
			logger.error("Unknown Captive portal. Cannot authenticate.");
			return false;
		}

		String authHost = url.getHost();
		if (authParams == null) {
			authParams = getStoredAuthParams(authHost);

			if (parsedPage.checkParamsMissing(authParams)){
				wifiDb = null;
				requestUserParams (url, parsedPage);
				// we will have to try authentication directly from user-facing activity
				return false;
			}
		}	
		
		boolean success = false;
		ParsedHttpInput result = postForm (url, parsedPage, authParams);
		// there could be a bunch of additional automated forms at the end
		while (result != null && result.submitOnLoad()) {
			logger.debug("Handling post-auth onLoad submit for [" + url + "]");
			result = postForm (url, result, null);
		}
		
		if (result != null) {
			logger.debug("Re-checking connection ...");
			URLRedirectChecker checker = new URLRedirectChecker (logger, context);
			success = checker.checkHttpConnection (AuthorizationType.None);
			if (success && wifiDb != null)
				wifiDb.storeAuthParams(authHost, authParams);
			if (success && context != null) {
				cancelWatchdogNotification();
				try {
					Toast.makeText(context, context.getText(R.string.success_notification) + " " + url.getHost(), Toast.LENGTH_SHORT).show();
				}catch (Throwable e){
					// don't care
				}
				// do we need this so that Toast would actually display ?
				Thread.yield();
			}
		}
		return success;
	}

}
