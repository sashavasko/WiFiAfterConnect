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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WifiAuthContent extends ContentProvider {

	public static final String AUTHORITY = "com.wifiafterconnect.data";
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	
	public static final Uri CONTENT_URI = BASE_URI;

	private WifiAuthDatabase wifiDb;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = wifiDb.getWritableDatabase();
		int count = db.delete(wifiDb.getTableName(uri), selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return WifiAuthDatabase.WIFI_HOSTS_CONTENT_TYPE_DIR;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// don't allow inserts for now
		return null;
	}

	@Override
	public boolean onCreate() {
		wifiDb = WifiAuthDatabase.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = wifiDb.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(wifiDb.getTableName (uri));
		return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// don't allow updates for now
		return 0;
	}

}
