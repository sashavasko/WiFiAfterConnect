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

import com.wifiafterconnect.util.WifiTools;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class CheckRedirectService extends IntentService {

	public CheckRedirectService(String name) {
		super(name);
	}

	public CheckRedirectService() {
		super("CheckRedirectService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(getString(R.string.action_check_redirect))) {
       		if (WifiTools.isWifiConnected(this)){
       			URLRedirectChecker checker = new URLRedirectChecker (Constants.TAG, getApplicationContext());
       			checker.setSaveLogFile (null);
       			checker.checkHttpConnection ();
        	}
        }
	}
	
	public static void startService (Context context) {
		Intent actionIntent = new Intent(context, CheckRedirectService.class);
		actionIntent.setAction (context.getString(R.string.action_check_redirect));
		context.startService (actionIntent);
	}

}
