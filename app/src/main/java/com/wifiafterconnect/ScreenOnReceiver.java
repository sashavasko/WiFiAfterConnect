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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ScreenOnReceiver extends BroadcastReceiver {
	
	static ScreenOnReceiver instance = null;
	
	private ScreenOnReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			try {
				// we may get onReceive after we already unregistered ourselves!
				context.unregisterReceiver (this); // we only do one-off thing
			} catch(IllegalArgumentException e) {}
			CheckRedirectService.startService (context);
			// will only get here when notification received in background,
			// and receiver got disabled as the result. 
			// So re-enable it!
			WifiBroadcastReceiver.setEnabled (context, true);			
        }
	}
	
	public static synchronized ScreenOnReceiver getInstance() {
		if (instance == null)
			instance = new ScreenOnReceiver();
		return instance;
	}
	
	public static boolean register(Context context) {
		if (context != null) {
			IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			try {
				context.getApplicationContext().registerReceiver(getInstance(), intentFilter);
				return true;
			} catch(IllegalArgumentException e) 
			{	// we are already registered
			}
		}
		return false;
	}

}
