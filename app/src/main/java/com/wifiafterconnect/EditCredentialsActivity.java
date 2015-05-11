package com.wifiafterconnect;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.wifiafterconnect.html.HtmlInput;

public class EditCredentialsActivity extends FragmentActivity {

	private WifiAuthParams authParams; 
   	private CheckBox checkSavePassword = null;
   	private TableLayout fieldsTable = null;
   	private ArrayList<View> edits = new ArrayList<View>();
   	private String authHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_auth_edit_layout);
        
        Intent intent = getIntent();
        WifiAuthDatabase wifiDb = WifiAuthDatabase.getInstance(this);
        long siteId = intent.getIntExtra(WifiAuthenticator.OPTION_SITE_ID, -1);
        authParams = wifiDb.getAuthParams (siteId);

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
        
	}

	private void onSaveClick(View v) {
		for (View ev : edits)
			view2Params(ev);
		
		WifiAuthDatabase.getInstance(this).storeAuthParams (authHost, authParams);		
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
    				onSaveClick(v);
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

}
