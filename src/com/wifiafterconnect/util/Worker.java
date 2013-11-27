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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.wifiafterconnect.Constants;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Worker extends ContextHolder {
	private Logger logger;
	protected Preferences prefs;
	
	public Worker (Logger logger, Context context) {
		super (context);
		this.logger = logger;
		this.prefs = new Preferences (context);
	}
	
	public Worker (Worker other) {
		super (other);
		this.logger = other.logger;
		this.prefs = new Preferences (other.getContext());
	}
	
	public void exception (Throwable e) {
		if (logger!= null)
			logger.exception(e);
		else
			e.printStackTrace();
	}
	
	public void debug (String msg) {
		if (logger != null)
			logger.debug(msg);
	}
	
	public void error (String msg) {
		if (logger != null)
			logger.error(msg);
	}
	
	public boolean isSaveLogToFile() {
		return prefs.getSaveLogToFile();
	}
	
	public void setLogFileName (String filename) {
		if (prefs.getSaveLogToFile()) {
			File saveDir = prefs.getSaveLogLocation();
			Log.d(Constants.TAG, "Save location = " + saveDir);
			try {
				logger.setLogFile(new File (saveDir, filename));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void toIntent (Intent intent) {
		if (logger != null)
			logger.toIntent(intent);
	}

}
