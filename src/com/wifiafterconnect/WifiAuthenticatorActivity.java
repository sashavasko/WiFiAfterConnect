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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.wifiafterconnect.html.HtmlInput;
import com.wifiafterconnect.util.Logger;
import com.wifiafterconnect.util.WifiTools;
import com.wifiafterconnect.util.Worker;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class WifiAuthenticatorActivity extends FragmentActivity 
										implements DisableWifiDialogFragment.DisableWifiDialogListener {

	private URL url = null;
   	private WifiAuthenticator wifiAuth = null;
   	private WifiAuthParams authParams = null;
   	private ParsedHttpInput parsedPage = null;

   	private CheckBox checkSavePassword = null;
   	private Button buttonAuthenticate = null;
   	private CheckBox checkAlwaysDoThat = null;
   	private TableLayout fieldsTable = null;
   	private ArrayList<View> edits = new ArrayList<View>();
   	
	public class RunAuthTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = wifiAuth.attemptAuthentication(parsedPage, authParams);
			return result;
		}
		
    	@Override
        protected void onPostExecute(Boolean result) {
    		if (result)
    			finish();
    		else {
    			Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
    			buttonAuthenticate.setEnabled(true);
    		}
        }
	}

	public void onAuthenticateClick(View v) {
		
		for (View ev : edits)
			view2Params(ev);
		
		if (checkSavePassword != null)
			authParams.savePassword = checkSavePassword.isChecked();
		
		buttonAuthenticate.setEnabled(false);
		
		RunAuthTask task = new RunAuthTask();
		task.execute();
	}
	
	protected boolean isAlwaysDoThat() {
		return (checkAlwaysDoThat != null && checkAlwaysDoThat.isChecked());
	}
	
	protected void performAction (WifiAuthenticator.AuthAction action) {
		if (action == WifiAuthenticator.AuthAction.DEFAULT)
			return;
		
		if (authParams.authAction != action && isAlwaysDoThat()) {
			authParams.authAction = action;
			wifiAuth.storeAuthAction(action);
		}
		switch (action) {
			case BROWSER : 
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.toURI().toString())));
				} catch (URISyntaxException e) {	}
				finish();
				break;
			case IGNORE : 
				if (authParams.wifiAction.perform(this))
					finish();
				else {
					DialogFragment dialogDiableWifi = new DisableWifiDialogFragment();
					dialogDiableWifi.show (getSupportFragmentManager(),"DisableWifiDialogFragment");
				}
				break;
		default:
			break;
		}
	}

	public void onCancelClick(View v) {
		performAction (WifiAuthenticator.AuthAction.IGNORE);
	}

	public void onOpenBrowserClick(View v) {
		performAction (WifiAuthenticator.AuthAction.BROWSER);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_auth_layout);
        
        Intent intent = getIntent();
  	   	
  	   	try {
			url = new URL (intent.getStringExtra (WifiAuthenticator.OPTION_URL));
		} catch (MalformedURLException e) {
			// TODO
		}
  	   	String html = intent.getStringExtra (WifiAuthenticator.OPTION_PAGE);
  	   	
  	   	wifiAuth = new WifiAuthenticator (new Worker (new Logger (intent), this), url);
  	   	parsedPage = new ParsedHttpInput (wifiAuth, url, html, new HashMap<String,String>());
  	   	authParams = wifiAuth.getStoredAuthParams();
   		authParams = parsedPage.addMissingParams(authParams);
   		
   		fieldsTable = (TableLayout)findViewById(R.id.fieldsTableLayout);
   		fieldsTable.removeAllViews();
   		edits.clear();
   		Log.d(Constants.TAG, "Adding controls...");
   		HtmlInput passwordField = authParams.getFieldByType(HtmlInput.TYPE_PASSWORD);
  	   	for (HtmlInput i : authParams.getFields()) {
  	   		if (i != passwordField)
  	   			addField (i);
  	   	}

  	   	checkSavePassword = (CheckBox)findViewById(R.id.checkSavePassword);

  	   	if (passwordField != null)
  	   		addField (passwordField);
  	   	else if (checkSavePassword != null) {
   			checkSavePassword.setVisibility (View.GONE);
   			checkSavePassword = null;
   		}
  	   	
    	buttonAuthenticate = (Button) findViewById(R.id.buttonAuthenticate);
    	checkAlwaysDoThat = (CheckBox)findViewById(R.id.checkAlwaysDoThat);
    	
    	performAction (authParams.authAction); 
    }
    
    private void view2Params (View v) {
		if (v instanceof EditText && authParams != null) {
			EditText edit = (EditText)v;
			String tag = (String) v.getTag();
			HtmlInput field = authParams.getField (tag);
			if (field != null)
				field.setValue(edit.getText().toString().trim());
		}
    }
    
    private void addField (HtmlInput field) {
		Log.d(Constants.TAG, "adding ["+field.getName() + "], type = [" + field.getType()+"]");

    	TextView labelView =  new TextView(this);
    	labelView.setText(field.getName());
    	int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, (float) 8, getResources().getDisplayMetrics());
    	labelView.setTextSize (textSize);
    	
    	EditText editView = new EditText(this);
    	editView.setInputType(field.getAndroidInputType());
    	editView.setText (field.getValue());
    	editView.setTag(field.getName());
    	editView.setFocusable (true);
    	
    	edits.add(editView);
    	
    	editView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
    			if (actionId == EditorInfo.IME_ACTION_DONE) {
    				onAuthenticateClick(v);
    			}
    			return false;
			}
    	});    	
    	
    	TableRow row = new TableRow (this);
 		fieldsTable.addView (row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT));

    	TableRow.LayoutParams labelLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
    	int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 5, getResources().getDisplayMetrics());
    	labelLayout.setMargins(margin, margin, margin, margin);
    	row.addView(labelView, labelLayout);
    	TableRow.LayoutParams editLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
    	row.addView(editView, editLayout);
    	
    }
    
	@Override
	public void saveAction(WifiTools.Action action) {
		wifiAuth.storeWifiAction (action);
	}

}
