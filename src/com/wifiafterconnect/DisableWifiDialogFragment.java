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

import com.wifiafterconnect.util.WifiTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.CheckBox;

public class DisableWifiDialogFragment extends DialogFragment {

	public interface DisableWifiDialogListener {
		public void saveAction (WifiTools.Action action);
	}
	
	protected void performAction (WifiTools.Action action) {
		Activity activity = getActivity();
		DisableWifiDialogListener listener = (DisableWifiDialogListener)activity;
		if (activity != null) {
			action.perform(activity);
			if (listener != null) {
				CheckBox checkAlways = (CheckBox)activity.findViewById(R.id.checkAlwaysDoThat);
				if (checkAlways != null && checkAlways.isChecked())
					listener.saveAction (action);
			}
			activity.finish();
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.title_disable_wifi);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.disable_wifi_dlg,null));

		builder.setPositiveButton (R.string.button_disable_wifi, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				performAction(WifiTools.Action.DISABLE);
			}
		});
		
		builder.setNeutralButton (R.string.button_keep_wifi, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				performAction(WifiTools.Action.KEEP);
			}
		});
		
		return builder.create();
	}
}
