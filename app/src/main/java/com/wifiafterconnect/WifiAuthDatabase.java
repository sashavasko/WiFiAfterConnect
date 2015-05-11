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

import java.util.Locale;

import com.wifiafterconnect.WifiAuthenticator.AuthAction;
import com.wifiafterconnect.html.HtmlInput;
import com.wifiafterconnect.util.WifiTools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WifiAuthDatabase extends SQLiteOpenHelper {

	private static WifiAuthDatabase instance = null;
	
	private static final String DATABASE_NAME = "wifiauth.db";
	private static final int DATABASE_VERSION = 7;

	public static final String WIFI_TABLE_NAME = "WifiHosts";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_HOSTNAME = "HostName";
	public static final String COLUMN_AUTH_ACTION = "AuthAction";
	public static final String COLUMN_WIFI_ACTION = "WifiAction";
	static final String[] WIFI_TABLE_PROJECTION_ALL = {COLUMN_ID, COLUMN_AUTH_ACTION, COLUMN_WIFI_ACTION};
	
	public static final String WIFI_AUTH_PARAMS_TABLE_NAME = "WifiAuthParams";

	public static final String COLUMN_HOST_ID = "HostId";
	public static final String COLUMN_PARAM_NAME = "Name";
	public static final String COLUMN_PARAM_TYPE = "Type";
	public static final String COLUMN_PARAM_VALUE = "Value";
	
	public static final String KNOWN_SSIDS_TABLE_NAME = "KnownSSIDs";

	public static final String COLUMN_SSID = "SSID";

	public final static String WIFI_HOSTS_CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.wifiafterconnect.wifihosts";
	
	/**
	 *  As per http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html :
	  * Making Constructor private to prevent direct instantiation.
	  * make call to static factory method "getInstance()" instead.
	  */
	private WifiAuthDatabase(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static WifiAuthDatabase getInstance(Context ctx) {
		// Use the application context, which will ensure that you 
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (instance == null && ctx != null) {
			instance = new WifiAuthDatabase(ctx.getApplicationContext());
		}
		return instance;
	}
	
	protected SQLiteDatabase getDb() {
		return getWritableDatabase();
	}

	public String getTableName(Uri uri) {
		// may need to be extended in future if we have more then one table
		return WIFI_TABLE_NAME;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + WIFI_TABLE_NAME+ " ( " +
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_HOSTNAME + " TEXT, " + 
				COLUMN_AUTH_ACTION + " TEXT, " +
				COLUMN_WIFI_ACTION + " TEXT, " +
				"UNIQUE (" + COLUMN_ID + ") ON CONFLICT REPLACE)"
			);
		db.execSQL("CREATE TABLE " + WIFI_AUTH_PARAMS_TABLE_NAME+ " ( " +
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_HOST_ID + " INTEGER, " + 
				COLUMN_PARAM_NAME + " TEXT, " +
				COLUMN_PARAM_TYPE + " TEXT, " +
				COLUMN_PARAM_VALUE + " TEXT, " +
				" FOREIGN KEY (" + COLUMN_HOST_ID + ") REFERENCES " + WIFI_TABLE_NAME+ " (" + COLUMN_ID + ") ON DELETE CASCADE," +
				" UNIQUE (" + COLUMN_ID + ") ON CONFLICT REPLACE)"
			);
		db.execSQL("CREATE TABLE " + KNOWN_SSIDS_TABLE_NAME+ " ( " +
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				COLUMN_SSID + " TEXT, " +
				" UNIQUE (" + COLUMN_ID + ") ON CONFLICT REPLACE)"
			);
		db.execSQL("insert Into " + WIFI_TABLE_NAME + " values (1,'www.test1.com','" + AuthAction.DEFAULT + "','" +  WifiTools.Action.DEFAULT + "')");
		db.execSQL("insert Into " + WIFI_AUTH_PARAMS_TABLE_NAME + " values (1,1,'" + WifiAuthParams.USERNAME + "','" + HtmlInput.TYPE_TEXT + "','sasha')");
		db.execSQL("insert Into " + WIFI_AUTH_PARAMS_TABLE_NAME + " values (2,1,'" + WifiAuthParams.PASSWORD + "','" + HtmlInput.TYPE_PASSWORD + "','secret')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion <= newVersion){
        	db.execSQL("DROP TABLE IF EXISTS " + WIFI_TABLE_NAME);
        	db.execSQL("DROP TABLE IF EXISTS " + WIFI_AUTH_PARAMS_TABLE_NAME);
        	db.execSQL("DROP TABLE IF EXISTS " + KNOWN_SSIDS_TABLE_NAME);
        	onCreate(db);
        }
	}
	
	public Cursor getWifiTableCursor (final String[] projection, final String authHost) {
		SQLiteDatabase db = getDb();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(WIFI_TABLE_NAME);
		Cursor c;
		if (authHost == null || authHost.isEmpty())
			c = builder.query(db, projection, null, null, null, null, null);
		else 
			c = builder.query(db, projection, COLUMN_HOSTNAME + " = ?", new String[] {authHost}, null, null, null); 

		// SQLite cursors are always not-null positioned before the first item 
		return c;	
	}

	public Cursor getWifiTableCursor (final String[] projection, final Long hostId) {
		SQLiteDatabase db = getDb();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(WIFI_TABLE_NAME);
		return builder.query(db, projection, COLUMN_ID + " = ?", new String[] {hostId.toString()}, null, null, null); 
	}
	
	public Cursor getAuthParamsCursor (final String[] projection, long hostId) {
		SQLiteDatabase db = getDb();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(WIFI_AUTH_PARAMS_TABLE_NAME);
		return builder.query(db, projection, COLUMN_HOST_ID + " = ?", new String[] {Long.toString(hostId)}, null, null, null); 
	}

	public long getWifiHostId (final String authHost) {
		long id = -1;
		if (authHost != null && !authHost.isEmpty()) {
			Cursor c = getWifiTableCursor (new String[]{COLUMN_ID}, authHost);
			if (c.moveToFirst()) {
				id = c.getLong(0);
			}
			c.close();
		}
		return id;
	}
	
	public long getKnownSSID (final String ssid) {
		SQLiteDatabase db = getDb();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(KNOWN_SSIDS_TABLE_NAME);
		Cursor c;
		c = builder.query(db, new String[]{COLUMN_ID}, COLUMN_SSID + " = ?", new String[] {ssid}, null, null, null); 
		long id = -1;
		if (c.moveToFirst()) {
			id = c.getLong(0);
		}
		c.close();
		return id;
	}

	public long updateWifiTable (final String authHost, final ContentValues values) {
		if (authHost == null || authHost.isEmpty())
			return -1;
		
		long hostId = getWifiHostId (authHost);
		SQLiteDatabase db = getDb();
		if (hostId < 0) {
			ContentValues valuesInsert = values == null ? new ContentValues() : values;
			if (!valuesInsert.containsKey(COLUMN_HOSTNAME))
				valuesInsert.put(COLUMN_HOSTNAME, authHost);
			return db.insert(WIFI_TABLE_NAME, null, valuesInsert);
		}
		if (values != null)
			db.update(WIFI_TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{Long.toString(hostId)});
		return hostId;
	}

	private long getAuthParamRowId (SQLiteDatabase db, long hostId, String name){
		long rowId = -1;
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(WIFI_AUTH_PARAMS_TABLE_NAME);
		Cursor c = builder.query(db, new String[] {COLUMN_ID}, COLUMN_HOST_ID + " = ? And " + COLUMN_PARAM_NAME + " = ?", new String[] {Long.toString(hostId), name}, null, null, null); 
		if (c.moveToFirst())
			rowId = c.getLong(0);
		c.close();
		return rowId;
	}
	
	private long updateAuthParamTable (SQLiteDatabase db, long hostId, final String name, final ContentValues  values) {
		if (values == null || values.size() == 0 || hostId < 0)
			return -1;

		long rowId = getAuthParamRowId (db, hostId, name);
		if (rowId < 0) {
			if (!values.containsKey(COLUMN_HOST_ID))
				values.put(COLUMN_HOST_ID, hostId);
			return db.insert(WIFI_AUTH_PARAMS_TABLE_NAME, null, values);
		}
		
		db.update(WIFI_AUTH_PARAMS_TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{Long.toString(rowId)});
		return rowId; 
	}
	
	
	public void storeAuthAction (final String authHost, final AuthAction action) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_AUTH_ACTION, action.toString());
		updateWifiTable (authHost, values);
	}

	public AuthAction getAuthAction (final String authHost) {
		AuthAction action = AuthAction.DEFAULT;
		final String[] projection = {COLUMN_AUTH_ACTION};
		Cursor c = getWifiTableCursor (projection, authHost); 
		if (c.moveToFirst()) {
			action = AuthAction.parse (c.getString(0));
		}
		c.close();
		return action;
	}

	public void storeWifiAction (final String authHost, final WifiTools.Action action) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_WIFI_ACTION, action.toString());
		updateWifiTable (authHost, values);
	}

	public WifiTools.Action getWifiAction (final String authHost) {
		WifiTools.Action action = WifiTools.Action.DEFAULT;
		final String[] projection = {COLUMN_WIFI_ACTION};
		Cursor c = getWifiTableCursor (projection, authHost); 
		if (c.moveToFirst()) {
			action = WifiTools.Action.parse (c.getString(0));
		}
		c.close();
		return action;
	}

	protected WifiAuthParams getAuthParamsFromCursorAndCloseIt (Cursor c, final Long hostId){
		WifiAuthParams params = new WifiAuthParams();
		params.authAction = AuthAction.parse (c.getString(1));
		params.wifiAction = WifiTools.Action.parse (c.getString(2));
		c.close();
		c = getAuthParamsCursor (new String[]{COLUMN_PARAM_NAME, COLUMN_PARAM_TYPE, COLUMN_PARAM_VALUE}, hostId);
		while (c.moveToNext()) {
			HtmlInput i = new HtmlInput (c.getString(0), c.getString(1), c.getString(2));
			params.add (i);
		}
		c.close();
		return params;
	}
	
	public WifiAuthParams getAuthParams (final String authHost) {

		Cursor c = getWifiTableCursor (WIFI_TABLE_PROJECTION_ALL, authHost);
		if (c.moveToFirst()) {
			long hostId = Long.parseLong(c.getString(0));
			return getAuthParamsFromCursorAndCloseIt(c, hostId);
		}
		return null;
	}

	public WifiAuthParams getAuthParams (final Long hostId) {
		Cursor c = getWifiTableCursor (WIFI_TABLE_PROJECTION_ALL, hostId);
		return c.moveToFirst() ? getAuthParamsFromCursorAndCloseIt(c, hostId) : null;
	}
	
	public void storeAuthParams (final String authHost, final WifiAuthParams params) {
		if (params != null) {
			long hostId = updateWifiTable (authHost, null);
			//Log.d(Constants.TAG, "Saving authParams for host [" + authHost + "], host id = " + hostId + "]");

			if (hostId >= 0) {
				SQLiteDatabase db = getDb();
				
				for (HtmlInput i : params.getFields()) {
					//Log.d(Constants.TAG, "Param name=[" + i.getName() + "], value = [" + i.getValue()+ "]");
					if (!i.matchType ("password") || params.savePassword) {
						ContentValues values = new ContentValues();
						values.put(COLUMN_PARAM_NAME, i.getName());
						values.put(COLUMN_PARAM_TYPE, i.getType().toLowerCase(Locale.ENGLISH));
						values.put(COLUMN_PARAM_VALUE, i.getValue());
						updateAuthParamTable (db, hostId, i.getName(), values);
					}
				}
			}
		}
	}

	public boolean isKnownSSID(String ssid) {
		return getKnownSSID (ssid) >= 0;
	}

	public void storeSSID(String ssid) {
		//Log.d(Constants.TAG, "Saving SSID [" + ssid + "]");
		if (!isKnownSSID(ssid)) {
			SQLiteDatabase db = getDb();
			ContentValues values = new ContentValues();
			values.put(COLUMN_SSID, ssid);
			db.insert(KNOWN_SSIDS_TABLE_NAME, null, values);
		}
	}
	
	public void deleteSite (long id) {
		SQLiteDatabase db = getDb();
		db.delete (WIFI_TABLE_NAME, COLUMN_ID + " = ?", new String[]{Long.toString(id)});
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }		
	}

}
