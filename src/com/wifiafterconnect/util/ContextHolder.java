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

import android.content.Context;
import android.content.Intent;

public class ContextHolder {

	private Context context = null;
	
	public ContextHolder (Context context) {
		this.context = context;
	}

	public ContextHolder (ContextHolder other) {
		this.context = other.context;
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getResourceString (int id) {
		return (context != null) ? context.getString(id) : "";
	}
	
	public Object getSystemService (String name) {
		return (context != null)? context.getSystemService(name) : null;
		
	}
	
	public Intent makeIntent (Class<?> cls) {
		return new Intent(context, cls);
	}
	
	public void startActivity (Intent intent) {
		if (context != null)
			context.startActivity(intent);
	}

	
}
