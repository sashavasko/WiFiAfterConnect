package com.wifiafterconnect.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class WISPAccessGatewayParam {

	enum MessageTypes { 
		Redirect,				//> 100 Initial redirect message
		Proxy,					//> 110 Proxy notification
		AuthenticationReply,	//> 120 PAP Authentication notification
		EAPAuthenticationReply,	//> 121 EAP Authentication notification
		LogoffReply,			//> 130 Logoff notification
		AuthenticationPollReply,//> 140 Deprecated (see note 1)
		AbortLoginReply,		//> 150 Response to Abort Login
		StatusReply,			//> 160 Response to Status query
		PollNotification;		//> 170 Authentication Poll notification
	}
	
	enum MessageElements {
		MessageType,		//> Positive Integer Indicate the type of the message
		ResponseCode,		//> Positive Integer Indicate the result of the requested execution
		@Deprecated
		AccessProcedure,	//> String 	Deprecated. The <AccessProcedure> element defined in WISPr 1.0 is deprecated and  
							//  		SHALL only be used if the access gateway also provides WISPr 1.0 access service.
		VersionLow,			//> WISPrVersion Indicates the lowest version of the WISPr protocol
		                    //      	supported by the access gateway
		VersionHigh,		//> WISPrVersion Indicates the highest version of the WISPr protocol
		                    //      	supported by the access gateway
		LocationName,		//> String 	Indicates a textual description of the hotspot or group
		                    // 			of hotspots to which the user is connected to
		AccessLocation,		//> String 	Indicates a key that uniquely identify a hotspot or
		                    //   		group of hotspots to which the user is connected to
		MaxSessionTime,		//> Positive Long Indicates in seconds the time left before the access
		                    //          gateway terminates the session.
		EAPMsg,				//> Base64 	Encoded Contains a Base 64 encoded EAP request message	Binary 
		Delay,				//> Positive Integer Indicates in seconds the time that the client software
		                    //    		must wait before sending the next WISPr request.
		ReplyMessage,		//> String 	Describes in a human readable format the cause of
		                    // 			an error. Reply messages can be displayed to the  user by the client software.
		AbortLoginURL,		//> URL 	Indicates the URL that can be requested to abort a
        					//			login request when the access gateway is using the polling mechanism.
		LoginURL,			//> URL 	Indicates the URL that must be requested to open a
							// 			session (authenticate the user)
		LogoffURL,			//> URL 	Indicates the URL that must be requested to terminate the session
		StatusURL,			//> URL 	Indicates the URL that can be requested to retrieve
							//			the status of the session
		RedirectionURL,		//> URL 	Indicates the URL in the client software must open in
         					// 			a browser upon successful login
		LoginResultsURL,	//> URL 	Indicates the URL that must be requested for polling
          					// 			to fetch the result of a pending authentication
		NextURL,			//> URL 	Indicates the URL that must be requested following a proxy operation
		OperatorURL,		//  URL 	Indicates the URL that must be requested to reach
							// 			ID=xxx> the infrastructure of an hosted VNP
		Status;				//> Integer Indicates the status of a session. Value 1 indicates
							// 			that the session is active. Value 0 indicates that the
							// 			session is terminated.
	}
	
	public static final int RESPONSE_NO_ERROR = 0;				//No error 
	public static final int RESPONSE_LOGIN_CHALLENGED = 10;		// Login challenged (Access CHALLENGE)
	public static final int RESPONSE_LOGIN_SUCCEEDED = 50;		// Login succeeded (Access ACCEPT)
	public static final int RESPONSE_LOGIN_FAILED = 100;		// Login failed (Access REJECT)
	public static final int RESPONSE_SERVER_ERROR = 102;		// Authentication server error/timeout
	public static final int RESPONSE_NO_AUTH_SERVER = 105;		// Network Administrator Error: No authentication server  enabled
	public static final int RESPONSE_LOGOFF_SUCCEEDED = 150;		// Logoff succeeded
	public static final int RESPONSE_LOGIN_ABORTED = 151;		// Login aborted
	public static final int RESPONSE_INVALID_SESSION = 152;		// Invalid session
	public static final int RESPONSE_PROXY_REPEAT = 200;		// Proxy detection/repeat operation
	@Deprecated
	public static final int RESPONSE_NOTIFICATION = 201;		// Deprecated (eplaced by the <PollNotification> message)
	public static final int RESPONSE_INVALID_STATE = 252;		// Invalid state for WISPr request
	public static final int RESPONSE_MTU_TOO_BIG = 253;		// MTU of AAA message is too big
	public static final int RESPONSE_PROTO_ERROR = 254;		// Protocol error
	public static final int RESPONSE_INTERNAL_ERROR = 255;		// Access gateway internal error

	public static final String USER_AGENT_1_0 = "CaptiveNetworkSupport/1.0 wispr";
	public static final String USER_AGENT_2_0 = "WISPR!WifiAfterConnect"; // per WISPr 2.0 spec 7.6
	
	private String accessProcedure = "";
	private String accessLocation = "";
	private String locationName = "";
	private String loginURL = "";
	private String messageType = "";
	private String responseCode = "";

	public static WISPAccessGatewayParam parse (String data) {
		WISPAccessGatewayParam wp = null;
		InputStream in = new ByteArrayInputStream(data.getBytes());
		try {
			wp = parse(in);
			in.close();
		}catch (IOException e) {
		}
		
		return wp;
	}

	public static WISPAccessGatewayParam parse (InputStream in) throws IOException {
		try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
			parser.nextTag();
            return parse(parser);
        } catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	// We don't use namespaces
    private static final String ns = null;

	public static WISPAccessGatewayParam parse(XmlPullParser parser) throws XmlPullParserException, IOException {
		WISPAccessGatewayParam wispr = null;
		parser.require(XmlPullParser.START_TAG, ns, "WISPAccessGatewayParam");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        // Starts by looking for the entry tag
	        if (parser.getName().equals("Redirect")) {
	        	if (wispr == null)
	        		wispr = new WISPAccessGatewayParam(); 
	        	wispr.parseRedirect (parser);
	        }else {
	        	skip(parser);
	        }
	    }  
	    return wispr;
	}

	private void parseRedirect (XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "Redirect");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        if (name.equals("AccessProcedure")) {
	        	accessProcedure = parseSimpleTag (parser, "AccessProcedure");
	        } else if (name.equals("AccessLocation")) {
	        	accessLocation = parseSimpleTag (parser, "AccessLocation");
	        } else if (name.equals("LocationName")) {
	        	locationName = parseSimpleTag (parser, "LocationName");
	        } else if (name.equals("LoginURL")) {
	        	loginURL = parseSimpleTag (parser, "LoginURL");
	        } else if (name.equals("MessageType")) {
	        	messageType = parseSimpleTag (parser, "MessageType");
	        } else if (name.equals("ResponseCode")) {
	        	responseCode = parseSimpleTag (parser, "ResponseCode");
	        } else {
	            skip(parser);
	        }
	    }
	}

	private static String parseSimpleTag(XmlPullParser parser, String name) throws XmlPullParserException, IOException {
		String result = "";
		parser.require(XmlPullParser.START_TAG, ns, name);
	    result = readText(parser);
	    parser.require(XmlPullParser.END_TAG, ns, name);
		return result;
	}

	// For the tags title and summary, extracts their text values.
	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
	    String result = "";
	    if (parser.next() == XmlPullParser.TEXT) {
	        result = parser.getText();
	        parser.nextTag();
	    }
	    return result;
	}

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
	    if (parser.getEventType() != XmlPullParser.START_TAG) {
	        throw new IllegalStateException();
	    }
	    int depth = 1;
	    while (depth != 0) {
	        switch (parser.next()) {
	        case XmlPullParser.END_TAG:
	            depth--;
	            break;
	        case XmlPullParser.START_TAG:
	            depth++;
	            break;
	        }
	    }
	 }

	@Override
	public String toString() {
		return "AccessProcedure = " + accessProcedure 
				+ "; AccessLocation = " + accessLocation
				+ "; LocationName = " + locationName
		        + "; LoginURL = " + loginURL
		        + "; MessageType = " + messageType
		        + "; ResponseCode = " + responseCode;
	}
    
    
}
