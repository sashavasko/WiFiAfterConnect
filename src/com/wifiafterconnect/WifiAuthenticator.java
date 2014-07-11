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

import java.net.URISyntaxException;
import java.net.URL;

import com.wifiafterconnect.URLRedirectChecker.AuthorizationType;
import com.wifiafterconnect.handlers.CaptivePageHandler;
import com.wifiafterconnect.util.Worker;
import com.wifiafterconnect.util.WifiTools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class WifiAuthenticator extends Worker{
	public static final String OPTION_URL = "PARAM_URL";
	public static final String OPTION_PAGE = "PARAM_PAGE";
	public static final String OPTION_AUTH_HOST = "PARAM_AUTH_HOST";
	public static final String OPTION_SITE_ID = "PARAM_SITE_ID";

	private static final String CAPTIVE_PORTAL_TRACKER_NOTIFICATION_ID = "CaptivePortal.Notification"; // Android 4.2 and later
	private static final String WALLED_GARDEN_NOTIFICATION_ID = "WifiWatchdog.walledgarden"; // Android 4.1
	private static final String WATCHDOG_NOTIFICATION_ID = "Android.System.WifiWatchdog"; // Android 4.0
	

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
	
	public enum AuthStatus {
		INPROGRESS, SUCCESS, FAIL;
		
		public static AuthStatus valueOf(boolean success) {
			return success ? SUCCESS: FAIL;
		}
		
		public static AuthStatus valueOf(CaptivePageHandler.States captiveResult){
			return captiveResult == CaptivePageHandler.States.Success ? SUCCESS : FAIL;
		}
		
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}
	
	private String authHost;
	
	public static String parseWifiHost (URL hostURL, Context context) {
		String authHost = hostURL.getHost();
		if (authHost.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}")) // raw IP address - supplement with WiFi SSID
		{
			String ssid = WifiTools.getSSID(context);
			if (ssid != null) {
				if (ssid.startsWith("\""))
					ssid = ssid.substring(1, ssid.length()-1);
				authHost = ssid + ":" + authHost;
			}
		}
		return authHost;
	}
	
	public WifiAuthenticator (Worker creator, URL hostURL) {
		super (creator);
		authHost = parseWifiHost(hostURL, getContext());
	}

	private WifiAuthDatabase getDb() {
		return WifiAuthDatabase.getInstance(getContext());
	}
	
	public WifiAuthParams getStoredAuthParams() {
		WifiAuthDatabase wifiDb = getDb();
		return wifiDb != null ? wifiDb.getAuthParams (authHost): null;
	}
	public void storeAuthAction (final AuthAction action) {
		WifiAuthDatabase wifiDb = getDb();
		if (wifiDb != null)
			wifiDb.storeAuthAction (authHost, action);
	}

	public AuthAction getAuthAction () {
		WifiAuthDatabase wifiDb = getDb();
		return wifiDb != null? wifiDb.getAuthAction (authHost): null;
	}

	public void storeWifiAction (final WifiTools.Action action) {
		WifiAuthDatabase wifiDb = getDb();
		if (wifiDb != null)
			wifiDb.storeWifiAction (authHost, action);
	}

	public WifiTools.Action getWifiAction () {
		WifiAuthDatabase wifiDb = getDb();
		return wifiDb != null? wifiDb.getWifiAction (authHost): null;
	}
	
	
	protected void notifyWifiDisabled() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
		.setSmallIcon (R.drawable.wifiac_small)//(android.R.drawable.presence_offline)
		.setContentTitle(getResourceString(R.string.notif_wifi_disabled_title))
		.setContentText(authHost + " - " + getResourceString(R.string.notif_wifi_disabled_text));
		
		Intent resultIntent = makeIntent(MainActivity.class);
		if (prefs.getReenableWifiQuiet())
			resultIntent.setAction(getResourceString(R.string.action_reenable_wifi));
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
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
		debug("posting notification that wifi was disabled (" + n.toString() + ")");

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, n);
	}
	
	protected void notifyAuthentication(AuthStatus status)
	{
		if (prefs.getNotifyAuthentication())
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
			.setSmallIcon (R.drawable.wifiac_small)
			.setContentTitle(getResourceString(R.string.notif_wifi_auth_title));
			if (status == AuthStatus.INPROGRESS)
				builder.setContentText(getResourceString(R.string.notif_wifi_auth_inprogress));
			else if (status == AuthStatus.SUCCESS)
				builder.setContentText(getResourceString(R.string.notif_wifi_auth_success) + " - " + authHost);
			else if (status == AuthStatus.FAIL)
				builder.setContentText(getResourceString(R.string.notif_wifi_auth_fail));
			Notification n = builder.build();
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(1, n);
		}
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
	
	public void cancelWatchdogNotification() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (nm != null) {
			nm.cancel(WATCHDOG_NOTIFICATION_ID, 1);
			nm.cancel(WALLED_GARDEN_NOTIFICATION_ID, 1);
			nm.cancel(CAPTIVE_PORTAL_TRACKER_NOTIFICATION_ID, 1);
		}
	}
	
	protected void requestUserParams (ParsedHttpInput parsedPage) {
		
		debug("Need user input for authentication credentials.");
		
		if (getContext() == null) {
			error ("Context is not set - cannot start WifiAuthenticationActivity");
			return;
		}
		// Need to check that screen is not off:
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
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
			boolean disableWifiOnLock = prefs.getAutoDisableWifi();
			
			debug("Screen is off and disableWifiOnLock is " + disableWifiOnLock);
			
			if (disableWifiOnLock) {
				WifiTools.disableWifi(getContext());
				notifyWifiDisabled();
			}else {
				ScreenOnReceiver.register(getContext());
				// don't want to receive repeat notifications - will re-enable when screen comes on
				WifiBroadcastReceiver.setEnabled(getContext(), false);				
			}
		}else {
			debug("Screen is on - Starting new activity.");
			/**
			 *  Screen is On - so proceeding displaying activity asking for credentials.
			 */
			Intent intent = makeIntent(WifiAuthenticatorActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra (OPTION_URL, parsedPage.getURL().toString());
			intent.putExtra (OPTION_PAGE, parsedPage.getHtml());
			toIntent(intent);
			debug("Starting activity for intent:"+intent.toString());
			startActivity (intent);
		}
	}

	/*
	 * Doing our best showing user the Terms And Conditions page if he/she has not seen it yet.
	 */
	public boolean checkTNCShown(ParsedHttpInput parsed) {
		String ssid = WifiTools.getSSID (getContext());
		WifiAuthDatabase wifiDb = getDb();
		if (wifiDb == null || ssid == null || ssid.isEmpty())
			return true;
		
		if (wifiDb.isKnownSSID (ssid))
			return true;
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		Context context = getContext();
		boolean urlOpened = false;
		if (pm.isScreenOn() && context != null) {
			try {
				debug("TNC not shown previously. Redirecting to page in browser.");
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(parsed.getURL().toURI().toString()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				urlOpened = true;
			} catch (URISyntaxException e) { // don't care
			}
		}else {
			// what should we do when the screen is locked???
		}
		wifiDb.storeSSID (ssid);
		return !urlOpened;
	}

	public boolean attemptAuthentication (ParsedHttpInput currentPage, WifiAuthParams authParams) {

		notifyAuthentication(AuthStatus.INPROGRESS);
		CaptivePageHandler.States captiveState = CaptivePageHandler.States.HandleRedirects; 
		/* Some portals supply as the first page ip, MAC etc 
		 * inside of the form that has to be submitted onLoad.
		 * Wandering WiFi is the worst offender.
		 */
		while (captiveState != CaptivePageHandler.States.Success && captiveState != CaptivePageHandler.States.Failed) {
			
			debug("Handling pre-auth redirects. parsedPage = " + currentPage);
			// don't want to do meta http-equiv=refresh here as it is used to detect browsers with no JS support
			// and display error requiring it
			if ((currentPage = currentPage.handleAutoRedirects (Constants.MAX_AUTOMATED_REQUESTS, false)) == null) {
				error ("Failed to follow the sequence of redirects...");
				notifyAuthentication(AuthStatus.FAIL);
				return false;
			}
			debug("Done handling pre-auth redirects. parsedPage = " + currentPage);

			if (!currentPage.isKnownCaptivePortal()) {
				error ("Unknown Captive portal. Aborting.");
				notifyAuthentication(AuthStatus.FAIL);
				return false;
			}

			if (!checkTNCShown(currentPage)) {
				notifyAuthentication(AuthStatus.FAIL);
				return false; 	// it is the first time that user connected to this SSID , 
				// so we let them go through the proper web authentication.
			}

			debug("Checking for missing inputs at [" + currentPage.getURL() + "]");
			if (authParams == null) {
				authParams = getStoredAuthParams();

				if (currentPage.checkParamsMissing(authParams)){
					requestUserParams (currentPage);
					// we will have to try authentication directly from user-facing activity
					return false;
				}
			}	

			debug("Attempting authentication at [" + currentPage.getURL() + "]");
			ParsedHttpInput nextPage = currentPage.authenticateCaptivePortal (authParams);
			captiveState = (nextPage == null) ? CaptivePageHandler.States.Failed : currentPage.getCaptivePortalState();
			currentPage = nextPage;
		}
		
		AuthStatus status = AuthStatus.valueOf(captiveState);

		if (status.isSuccess()) {
			debug("Re-checking connection ...");
			URLRedirectChecker checker = new URLRedirectChecker (this);
			status = AuthStatus.valueOf(checker.checkHttpConnection (AuthorizationType.None));
			if (status.isSuccess())
			{
				WifiAuthDatabase wifiDb = getDb();
				debug("Saving Auth Params. db = [" + wifiDb + "]");
				if (wifiDb != null)
					wifiDb.storeAuthParams(authHost, authParams);
				if (getContext() != null) {
					cancelWatchdogNotification();
					try {
						//Toast.makeText(context, context.getText(R.string.success_notification) + " " + authHost, Toast.LENGTH_SHORT).show();
					}catch (Throwable e){
						// don't care
					}
					// do we need this so that Toast would actually display ?
					Thread.yield();
				}
			}
		}
		
		notifyAuthentication(status);
		return status.isSuccess();
	}

}
