/*
 * Copyright (C) 2014 Sasha Vasko <sasha at aftercode dot net> 
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
package com.wifiafterconnect.http;

/**
 *  @author Sasha Vasko
 *
 */
public enum HttpConnectionFactory {
	INSTANCE;
	
	private String connectionClass;
	private boolean singleton = false;
	private HttpConnectionWrapper singletonInstance;
	
	public HttpConnectionWrapper getConnection() {
		if (connectionClass == null)
			return new HttpURLConnectionWrapper();
		if (singletonInstance != null) {
			singletonInstance.reset();
			return singletonInstance;
		}
		HttpConnectionWrapper conn = null;
		try {
			conn = (HttpConnectionWrapper) Class.forName(connectionClass).newInstance(); 
		}catch (InstantiationException e) {
			
		}catch (IllegalAccessException e) {
			
		}catch (ClassNotFoundException e) {
			
		}
		if (singleton)
			singletonInstance = conn;
		
		return conn;
	}
	
	public void setConnectionClass (String name, boolean singleton) {
		if (singletonInstance != null) {
			if (!singleton || name == null || !name.equals(connectionClass))
				singletonInstance = null;
		}
		connectionClass = name;
		this.singleton = singleton;
	}

	public void setConnectionInstance (HttpConnectionWrapper instance) {
		singletonInstance = instance;
		connectionClass = instance.getClass().getName();
		this.singleton = true;
	}
}
