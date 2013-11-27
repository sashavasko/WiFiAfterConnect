/**
 * 
 */
package com.wifiafterconnect.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author sasha
 *
 */
public class HttpInput {
	private String source = "";
	private URL url = null;
	
	public HttpInput (URL url){
		this.url = url;
	}
	
	public URL getURL () {
		return url;
	}

	public String getSource () {
		return source;
	}
	
	public boolean parse (String source) {
		this.source = (source == null) ? "" : source;
		return (!source.isEmpty());
	}
	
	public String getTitle() {
		return "";
	}

	public String getURLQueryVar (String varName) {
		if (url != null && varName != null && !varName.isEmpty()) {
			varName += "=";
			String query = url.getQuery();
			if (query != null) {
				for (String v : url.getQuery().split("[&]")) {
					if (v.startsWith(varName)) {
						try {
							return URLDecoder.decode(v.substring(varName.length()),"UTF-8");
						} catch (UnsupportedEncodingException e) {}
					}
				}
			}
		}
		return null;
	}
	
	public URL makeURL (String urlString) throws MalformedURLException {
		if (urlString == null || urlString.isEmpty())
			return null;
		try {
			return new URL (urlString);
		} catch (MalformedURLException e) {
			URL srcURL = getURL(); 
			if (urlString.startsWith("/")) {
				URL result = new URL(srcURL.getProtocol() + "://" + srcURL.getAuthority() + urlString);
				//Log.d(Constants.TAG, "Authority = {" + srcURL.getAuthority() + "}, result URL = {" + result.toString() + "}");
				return result;
			}else {
				String path = srcURL.getPath();
				int idx = path.lastIndexOf('/');
				if (idx < 0)
					throw new MalformedURLException ();
				return new URL(srcURL.getProtocol() + "://" + srcURL.getAuthority() +  path.substring(0, idx+1) + urlString);
			}
		}
	}

}
