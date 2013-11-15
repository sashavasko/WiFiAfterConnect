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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.wifiafterconnect.BuildConfig;

import android.content.Intent;
import android.util.Log;

public class Logger {

	
	private String tag;
	private File file = null;
	private String getTag() { return tag; }
	
	private PrintWriter logFile = null;
	
	public Logger (final String tag) {
		this.tag = (tag != null) ? (tag.length() > 32 ? tag.substring(0,32) : tag) : "";
	}
	
	public Logger (final Intent intent) {
		this.tag = intent.getStringExtra(OPTION_LOGGER_TAG);
		String filePath =intent.getStringExtra(OPTION_LOGGER_FILE); 
		if (filePath != null && !filePath.isEmpty()){
			try {
				setLogFile (new File (filePath));
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void setLogFile (File file) throws IOException {
		debug ("Switching to logging to file:" + file.getAbsolutePath());
		logFile = new PrintWriter (new BufferedWriter(new FileWriter(file, true)));
		this.file = file;
	}
	
	public void exception (Throwable e) {
		if (logFile == null)
			e.printStackTrace();
		else {
			e.printStackTrace(logFile);
			logFile.println();
			logFile.flush();
		}
		
	}
	
	public void debug ( String s) {
		if (BuildConfig.DEBUG) {
			if (logFile == null)
				Log.d(getTag(), s);
			else {
				logFile.println(s);
				logFile.flush();
			}
		}
	}

	public void error ( String s) {
		if (logFile == null)
			Log.e(getTag(), s);
		else {
			logFile.println("Error:" + s);
			logFile.flush();
		}
	}
	
	private static final String OPTION_LOGGER_TAG = "PARAM_LOGGER_TAG";
	private static final String OPTION_LOGGER_FILE = "PARAM_LOGGER_FILE";
	
	public void toIntent (Intent intent) {
		intent.putExtra(OPTION_LOGGER_TAG, tag);
		if (file != null)
			intent.putExtra(OPTION_LOGGER_FILE, file.getAbsolutePath());
	}
	
	
}
