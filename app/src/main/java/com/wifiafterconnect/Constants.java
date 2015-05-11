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

public interface Constants {

	public static final String TAG = "WifiAfterConnect";
	
	public static final String URL_KERNEL_ORG_HTTPS = "https://mirrors.kernel.org/debian/pool/";
	public static final String URL_KERNEL_ORG_HTTP = "http://mirrors.kernel.org/debian/pool/";
	public static final String URL_GOOGLE_HTTP = "http://www.google.com";
	public static final String URL_GOOGLE_HTTPS = "https://www.google.com";
	
	// Google page is rather large, so let's try and pull a smaller page
	// kernel.org should be sufficiently fast and stable for our purposes
	public static final String URL_TO_CHECK_HTTP = URL_KERNEL_ORG_HTTP;
	public static final String URL_TO_CHECK_HTTPS = URL_KERNEL_ORG_HTTPS;
	
	public static final int SOCKET_CONNECT_TIMEOUT_MS = 0; //Use TCP timeout
	public static final int SOCKET_READ_TIMEOUT_MS = 220000; // this could be really slow in some cases
	
	
	public static final String [] PROTOCOLS = {"http","https"};

	public static final int MAX_AUTOMATED_REQUESTS = 10;

	// MAX timeout while handling <meta http-equiv="refresh" content="timeout; url=url">
	public static final int MAX_REFRESH_TIMEOUT = 15;


}
