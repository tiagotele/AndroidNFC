package com.example.nfc_from_scratch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AddUriDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_uri_dialog);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_uri_dialog, menu);
		return true;
	}

}
