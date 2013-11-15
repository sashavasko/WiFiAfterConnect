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

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.wifiafterconnect.util.Logger;
import com.wifiafterconnect.util.Preferences;
import com.wifiafterconnect.util.Worker;

import android.content.Context;

public class URLRedirectChecker extends Worker{
	
	/* storing IP address saves on a number of roundtrips for ip lookup */
	public static class CachedURL {
		private InetAddress address = null;
		private String hostname = "";
		private URL url = null;
		
		public void setURL (URL newURL) {
			url = newURL;
			try {
				if (address == null || !hostname.equals (newURL.getHost())) {
					hostname = newURL.getHost();
					address = lookupHost (hostname);
				}
			} catch (NullPointerException e) {
			}
			if (address != null) {
				try {
					url = new URL (url.getProtocol(), address.getHostAddress(), url.getPort(), url.getFile());
				} catch (MalformedURLException e) {
				}
			}
				
		}
		
		public URL getURL () {
			return url;
		}
	}
	
	private static CachedURL urlToCheckHttp;
	private static CachedURL urlToCheckHttps;
	
	public enum AuthorizationType {
		None, IfNeeded, Force;
	}
	
	public AuthorizationType defaultType = AuthorizationType.IfNeeded;
	
	private void initURLs () {
		if (getContext() == null) {
			try {
				urlToCheckHttp.setURL(new URL(Constants.URL_TO_CHECK_HTTP));
				urlToCheckHttps.setURL(new URL(Constants.URL_TO_CHECK_HTTPS));
			} catch (MalformedURLException e) {
				exception (e);
			}
		}else {
			urlToCheckHttp.setURL(prefs.getURLToCheckHttp());
			urlToCheckHttps.setURL(prefs.getURLToCheckHttps());
		}
	}
	
	static {
		CookieHandler.setDefault (new CookieManager (null, CookiePolicy.ACCEPT_ALL));
		urlToCheckHttp = new CachedURL();
		urlToCheckHttps = new CachedURL();
		// we cannot follow the redirects as we need the WISPr data. 
		// If no WISPr detected then redirects will be followed while authenticating 
		HttpURLConnection.setFollowRedirects(!Preferences.getWISPrEnabled());
		
		try {
			TrustManager[] trustAllCerts = { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() { 
					return null;
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
					// TODO Auto-generated method stub
					
				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			HostnameVerifier hv = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			sc.init(null, trustAllCerts, new SecureRandom());
			 
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			} catch (Exception localException) {
			}		
	}
	
	public URLRedirectChecker(Logger logger, Context context) {
		super (logger, context);
		initURLs ();
	}
	
	public URLRedirectChecker(String tag, Context context) {
		super (new Logger (tag == null ? "URLRedirectChecker" : tag), context);
		initURLs ();
	}
	
	public URLRedirectChecker(Worker creator) {
		super (creator);
		initURLs ();
	}
	
	public boolean attemptAuthorization (ParsedHttpInput parsedPage) {
		WifiAuthenticator auth = new WifiAuthenticator (this, parsedPage.getURL());
		return auth.attemptAuthentication (parsedPage, null);
	}
	

	public void setSaveLogFile (URL url) {
		setLogFileName ((url == null ? "probing" : url.getHost()) + ".log");
	}
	
	/* ###################################################### 
	 * The Captive Portal check code from android. Unlike them,
	 * we actually need the portal page, so that we can post a response.
	 * 
	 * Copyright (C) 2012 The Android Open Source Project
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 */
	private static final int SOCKET_TIMEOUT_MS = 10000;
	private static final String DEFAULT_SERVER = "clients3.google.com";

	public boolean isCaptivePortal(InetAddress server) {
        HttpURLConnection urlConnection = null;
        //if (!mIsCaptivePortalCheckEnabled) return false;

        String url_string = "http://" + server.getHostAddress() + "/generate_204";
        //if (DBG) log("Checking " + url_string);
        try {
            URL url = new URL(url_string);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // we got a valid response, but not from the real google
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {
            //if (DBG) log("Probably not a portal: exception " + e);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

	public static InetAddress lookupHost(String hostname) {
        InetAddress inetAddress[];
        try {
            inetAddress = InetAddress.getAllByName(hostname);
        } catch (UnknownHostException e) {
            return null;
        }

        for (InetAddress a : inetAddress) {
            if (a instanceof Inet4Address) return a;
        }
        return null;
    }
	
	/* ======================================================
	 * End of the The Captive Portal check code from android
	 */

	public boolean isCaptivePortal(String server_hostname) {
		InetAddress server = lookupHost (server_hostname == null ? DEFAULT_SERVER : server_hostname);
		return isCaptivePortal (server);
	}
	
	public boolean checkHttpConnection (URL url, AuthorizationType doAuthorize) {
		//if (proto < 0 || proto >= protocols.length)			return false;
		//String protocol = protocols[proto];
		boolean success = false;
		
		/* per WISPR 2.0 specs #7.7 must use HTTP for initial get
		 * This may not be optimal for other gateways as they redirect to HTTPS for authentication
		 */
		
		if (url == null)
			url = urlToCheckHttps.getURL(); 
			
		
		try {
			
			//URL url = new URL(protocol + "://www.google.com");
			ParsedHttpInput parsed = null;
			
			// due to switching to wifi, name resolution can fail if the timing in just right,
			// give it another chance
			for ( int i = 0; i < 2 && parsed == null ; ++i ) {
				if ((parsed = ParsedHttpInput.get (this, url, null)) == null) {
					try {	Thread.sleep(100); } catch (InterruptedException e) {} // don't care
				}
			}
			if (parsed == null)
				return false;
			
		    String field = null;
		    URL redirectURL = null;
		    
		    if (doAuthorize == AuthorizationType.Force) {
		    	success = attemptAuthorization (parsed);
		    }else if (!url.getHost().equals(parsed.getURL().getHost())) {
		        // we were redirected! Kick the user out to the browser to sign on?
		    	debug("Redirected to  [" + parsed.getURL() + "]");
		    	if (doAuthorize != AuthorizationType.None) {
		    		setSaveLogFile (parsed.getURL());
		    		success = attemptAuthorization (parsed);
		    	}
		    }else if (!(field = parsed.getHttpHeader(ParsedHttpInput.HTTP_HEADER_LOCATION)).isEmpty()){
		    	redirectURL = new URL (field);
		    }else if (parsed.hasMetaRefresh()) {
		    	redirectURL = parsed.getMetaRefresh().getURL();
		    }else
		    	success = true;

		    if (redirectURL != null) {
		    	if (!redirectURL.getHost().equals(parsed.getURL().getHost())) {
		    		debug("Redirected to  [" + redirectURL + "]. Assuming Internet unavailable - probably a captive portal.");
		    		if (!redirectURL.getProtocol().equals(url.getProtocol())) {
		    			debug("protocol has changed!");
		    		}
		    		if (doAuthorize != AuthorizationType.None) {
			    		setSaveLogFile (redirectURL);
			    		
			    		debug("WISPr = [" + parsed.getWISPr() + "]");
			    		
		    			if (!Preferences.getWISPrEnabled() || parsed.getWISPr() == null)
		    				success = checkHttpConnection (redirectURL, AuthorizationType.Force);
		    			else
		    				success = attemptAuthorization (parsed);
		    		}
		    	} else {
			    	// something wicked happened otherwise
		    		error("Unexpected redirect URL  [" + redirectURL + "] - giving up.");
		    	}
		    }
		} catch (MalformedURLException e){
    		error("Redirected to a malformed url ");
    		exception (e);
		}
		return success;
	}

	
	public void setDefaultType (AuthorizationType type) {
		defaultType = type;
	}
	
	public boolean checkHttpConnection () {
		boolean success = checkHttpConnection (urlToCheckHttp.getURL(), defaultType);
		debug("Internet connection is " + (success ? "Available" : "Blocked by Captive portal"));
		return success;
	}

	public boolean checkHttpConnection (AuthorizationType authType) {
		boolean success = checkHttpConnection (urlToCheckHttp.getURL(), authType);
		debug("Internet connection is " + (success ? "Available" : "Blocked by Captive portal"));
		return success;
	}
	
}
