package com.hajau.simplekeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.provider.Settings;
public class MainActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

	}

	public void onClick(View v){
		switch(v.getId()){
		case R.id.active:
			Intent active_setting = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
			active_setting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainActivity.this.startActivity(active_setting);
			break;
		case R.id.select:
			InputMethodManager imm = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showInputMethodPicker();
			break;
		}
	}
}
