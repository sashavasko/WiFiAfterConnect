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

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import com.wifiafterconnect.URLRedirectChecker.AuthorizationType;
import com.wifiafterconnect.util.WifiTools;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;
	private ListView lvRememberedSites;
	private Button deleteSelected;
	private WifiAuthDatabase wifiDb = null;
	private WifiTools wifiTools = null;
	private ToggleButton toggleWifi;
	private Button buttonAuthenticateNow;
	public enum InternetStatus { UNKNOWN, CHECKING, ONLINE, OFFLINE };
	public InternetStatus inetStatus = InternetStatus.UNKNOWN;
	private TextView inetStatusInd;
    private WifiBroadcastReceiver receiverWifiConnect = null;
    
	
	public class RedirectCheckerTask extends AsyncTask<Context, Void, Boolean> {
		@Override
	    protected void onPostExecute(Boolean result) {
	    	setInternetStatus (result ? InternetStatus.ONLINE : InternetStatus.OFFLINE);
	    	setAuthenticateNowEnabled();
	    }

	    public URLRedirectChecker configureChecker (URLRedirectChecker checker) {
	    	return checker;
	    }
	    
		@Override
		protected Boolean doInBackground(Context... params) {
			URLRedirectChecker checker = configureChecker (new URLRedirectChecker (Constants.TAG, params[0]));
			return checker.checkHttpConnection ();
		}
	}

	public class RunAuthTask extends RedirectCheckerTask {
		@Override
		public URLRedirectChecker configureChecker (URLRedirectChecker checker) {
			checker.setSaveLogFile (null);
			return checker;
		}
	}
	
	public class CheckOnlineAuthTask  extends RedirectCheckerTask {
		@Override
		public URLRedirectChecker configureChecker (URLRedirectChecker checker) {
			checker.setDefaultType (AuthorizationType.None);
			return checker;
		}
	}
	
	private void setInternetStatus (InternetStatus status) {
		if (inetStatus != status) {
			inetStatus = status;
			if (inetStatusInd != null) {
				switch (status) {
				case ONLINE : 
					inetStatusInd.setText(R.string.label_available);
					inetStatusInd.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); 
					break;
				case OFFLINE :
					inetStatusInd.setText(R.string.label_unavailable);
					inetStatusInd.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); 
					break;
				case UNKNOWN :
					checkInetOnline ();
				default : 
					inetStatusInd.setText(R.string.label_inet_checking);
					inetStatusInd.setTextColor(getResources().getColor(android.R.color.holo_orange_dark)); 
				}
			}
		}
	}
	
	public boolean checkInetOnline () {
		if (inetStatus == InternetStatus.UNKNOWN) {
			setInternetStatus (InternetStatus.CHECKING);
			//Log.d(Constants.TAG, "starting CheckOnlineAuthTask task");
			CheckOnlineAuthTask task = new CheckOnlineAuthTask();
			task.execute(this);
		}
		return (inetStatus == InternetStatus.ONLINE);
	}

	private void setAuthenticateNowEnabled (boolean enabled, int disabledLabelId) {
		if (buttonAuthenticateNow != null) {
			buttonAuthenticateNow.setEnabled(enabled);
			buttonAuthenticateNow.setText(disabledLabelId);
		}
	}

	private void setAuthenticateNowEnabled () {
		if (!wifiTools.isWifiConnected())
			setAuthenticateNowEnabled (false, R.string.auth_now_disabled_label);
		else {
			if (checkInetOnline()) // must be the first call as it changes the status
				setAuthenticateNowEnabled (false, R.string.auth_now_online_label);
			else if (inetStatus == InternetStatus.UNKNOWN 
					|| inetStatus == InternetStatus.CHECKING)
				setAuthenticateNowEnabled (false, R.string.auth_now_checking_label);
			else
				setAuthenticateNowEnabled (true, R.string.auth_now_label);
		}
	}
	
	public void onAuthenticateNowClick(View v) {
		if (wifiTools.isWifiConnected()) {
			setAuthenticateNowEnabled(false, R.string.auth_now_inprocess_label);
			RunAuthTask task = new RunAuthTask();
			task.execute(getBaseContext());
		}
	}

	public void onWifiToggle(View v) {
		CompoundButton btn = (CompoundButton)v;
		wifiTools.setWifiEnabled(btn.isChecked());
		if (!btn.isChecked())
			setAuthenticateNowEnabled(false, R.string.auth_now_disabled_label);;
	}

	public void onBakcgroundAuthEnabledToggle(View v) {
		CompoundButton btn = (CompoundButton)v;
		WifiBroadcastReceiver.setEnabled(this, btn.isChecked());
	}
	
	private Set<Long> selectedSiteIds = Collections.synchronizedSet(new HashSet<Long>());
	private Long lastSelectedSiteId = -1L;
	
	private class DeleteAndUpdateCursorTask extends AsyncTask<Void, Void, Cursor> {
	    protected void onPostExecute(Cursor result) {
	    	adapter.changeCursor(result);
	    	deleteSelected.setEnabled(false);
	    }

		@Override
		protected Cursor doInBackground(Void... params) {
			try {
				for (long id : selectedSiteIds)
					wifiDb.deleteSite (id);
			}catch(ConcurrentModificationException e){}
			selectedSiteIds.clear();
	        Cursor result = wifiDb.getWifiTableCursor (new String[] {WifiAuthDatabase.COLUMN_ID,WifiAuthDatabase.COLUMN_HOSTNAME}, (String)null);
	        return result;
		}
	}
	
	public void onDeleteSelectedClick(View v) {
		DeleteAndUpdateCursorTask delTask = new DeleteAndUpdateCursorTask();
		delTask.execute();
	}
	
	public void onEditCredentialsClick (View v) {
		if (!selectedSiteIds.contains(lastSelectedSiteId))
			return;
	
		Intent intent = new Intent(this, EditCredentialsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra (WifiAuthenticator.OPTION_SITE_ID, lastSelectedSiteId);
		startActivity (intent);
	}

	public void onSettingsClick(View v) {
		startActivity (new Intent(this, SettingsActivity.class));
	}

	public class WifiSitesCursorAdapter extends SimpleCursorAdapter {

		public class CheckBoxViewBinder implements SimpleCursorAdapter.ViewBinder {
			@Override
			public boolean setViewValue(View v, Cursor cursor, int field) {
				if (v instanceof CheckBox) {
					CheckBox cb = (CheckBox)v;
					long id = cursor.getLong(0);
					//Log.d(Constants.TAG, " site  id = [" + id + "]");
					cb.setTag(Long.valueOf(id));
					
					if (field != 0) {
						String val = cursor.getString(field);
						cb.setChecked(val!=null && val.equalsIgnoreCase("true"));
					}
					return true;
				} else if (v instanceof TextView) {
					String val = cursor.getString(field);
					//Log.d(Constants.TAG, "displaying site [" + val + "]");
					setViewText((TextView)v, val);
					return true;
				}
				return false;
			}
		
		}

		public WifiSitesCursorAdapter(Context context) {
			super(context, R.layout.sites_list_item, null, 
					new String[] {WifiAuthDatabase.COLUMN_ID, WifiAuthDatabase.COLUMN_HOSTNAME}, 
					new int[] {R.id.checkSelected, R.id.textSiteHost}, 0);
			setViewBinder (new CheckBoxViewBinder());
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
	        View v = super.getView(position, convertView, parent);
	        if (v != null) {
	            CheckBox cb = (CheckBox)v.findViewById (R.id.checkSelected);
	            if (cb != null) {
	                cb.setOnCheckedChangeListener(new OnCheckedChangeListener ()
	                	{@Override	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
	                		Long id = (Long)view.getTag();
	                		lastSelectedSiteId = id;
	                		if (isChecked)
	                			selectedSiteIds.add(id);
	                		else
	                			selectedSiteIds.remove(id);
	                		deleteSelected.setEnabled(!selectedSiteIds.isEmpty());
	                	}
	                });
	            }
	        }
	        return v;
	    }
	}

	protected void refreshStatusIndicators () {
        /*
          ToggleButton toggleBackAuth = (ToggleButton) findViewById(R.id.toggleEnableWifiAfterconnect);
        if (toggleBackAuth != null)
        	toggleBackAuth.setChecked(WifiBroadcastReceiver.isEnabled(this));
        */
		if (wifiTools != null) {
			boolean enabled = wifiTools.isWifiEnabled();
			//if (!enabled || !wifiTools.isWifiConnected())
			setInternetStatus (InternetStatus.UNKNOWN);
			setAuthenticateNowEnabled();
			if (toggleWifi != null)
				toggleWifi.setChecked(enabled);
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager nm =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
        wifiTools = WifiTools.getInstance(this);
        
        if (getIntent().getAction().equals(getString(R.string.action_reenable_wifi))) {
        	wifiTools.enableWifi();
        	finish();
        	return;
        }

        setContentView(R.layout.main_activity);
        
        toggleWifi = (ToggleButton) findViewById(R.id.toggleWifi);
//        toggleWifi.setEnabled(checkCallingOrSelfPermission("android.permission.CHANGE_NETWORK_STATE") 
//        						== PackageManager.PERMISSION_GRANTED);
        
        buttonAuthenticateNow = (Button) findViewById(R.id.buttonAuthenticateNow);
        
        lvRememberedSites = (ListView) findViewById(R.id.listKnownSites);
        deleteSelected = (Button)findViewById (R.id.buttonDeleteSelected);
        
        inetStatusInd = (TextView)findViewById (R.id.textInetStatusInd);
        
        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        adapter = new WifiSitesCursorAdapter(this);
        lvRememberedSites.setAdapter(adapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().initLoader(0, null, this);
        
        // WE would really like to keep track of connectivity state so that our buttons 
        // reflect the state correctly
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        receiverWifiConnect = new WifiBroadcastReceiver(){
    		@Override
    		public void onWifiConnectivityChange(Context context, boolean connected) {
    			refreshStatusIndicators ();
    		}
        };
        registerReceiver(receiverWifiConnect, intentFilter);
        
        // finally let us check if the device is connected to Internet presently
        checkInetOnline ();
    }

    @Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        adapter.swapCursor(null);	
    }

	public static class WifiSitesCursorLoader extends AsyncTaskLoader<Cursor> {

		WifiAuthDatabase wifiDb = null;
		Cursor cursor;
		
		public WifiSitesCursorLoader(Context context, WifiAuthDatabase wifiDb) {
			super(context);
			this.wifiDb = wifiDb;
		}

		@Override
		public Cursor loadInBackground() {
			Cursor c = wifiDb.getWifiTableCursor (new String[] {WifiAuthDatabase.COLUMN_ID,WifiAuthDatabase.COLUMN_HOSTNAME}, (String)null);
			if (c != null) {
				int count = c.getCount();
				Log.d(Constants.TAG, "Loaded " + count + " known sites");
			}else {
				Log.d(Constants.TAG, "Cursor = null loading known sites");
			}
			return c;
		}
		/* Runs on the UI thread */
	    @Override
	    public void deliverResult(Cursor cursor) {
	        if (isReset()) {
	            // An async query came in while the loader is stopped
	            if (cursor != null) {
	                cursor.close();
	            }
	            return;
	        }
	        Cursor oldCursor = this.cursor;
	        this.cursor = cursor;

	        if (isStarted()) {
	            super.deliverResult(cursor);
	        }

	        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
	            oldCursor.close();
	        }
	    }

	    /**
	     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
	     * will be called on the UI thread. If a previous load has been completed and is still valid
	     * the result may be passed to the callbacks immediately.
	     *
	     * Must be called from the UI thread
	     */
	    @Override
	    protected void onStartLoading() {
	        if (cursor != null) {
	            deliverResult(cursor);
	        }
	        if (takeContentChanged() || cursor == null) {
	            forceLoad();
	        }
	    }

	    /**
	     * Must be called from the UI thread
	     */
	    @Override
	    protected void onStopLoading() {
	        // Attempt to cancel the current load task if possible.
	        cancelLoad();
	    }

	    @Override
	    public void onCanceled(Cursor cursor) {
	        if (cursor != null && !cursor.isClosed()) {
	            cursor.close();
	        }
	    }

	    @Override
	    protected void onReset() {
	        super.onReset();
	        // Ensure the loader is stopped
	        onStopLoading();
	        if (cursor != null && !cursor.isClosed()) {
	            cursor.close();
	        }
	        cursor = null;
	    }
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
		wifiDb = WifiAuthDatabase.getInstance(this);
		return new WifiSitesCursorLoader(this, wifiDb);
        // return new CursorLoader(this, WifiAuthContent.CONTENT_URI, new String[] {WifiAuthDatabase.COLUMN_ID,WifiAuthDatabase.COLUMN_HOSTNAME}, null, null, null);
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	
    	if (item.getItemId() == R.id.action_settings) {
      	   	startActivity (new Intent(this, SettingsActivity.class));
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver (receiverWifiConnect);
		} catch(IllegalArgumentException e) {}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshStatusIndicators ();
		getSupportLoaderManager().restartLoader(0, null, this);
	}

}
