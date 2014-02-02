package com.example.nfc_from_scratch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddUriDialog extends Activity implements OnClickListener {

	EditText editTextUriFromUser;
	Button buttonAddUriFromUser, buttonCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_uri_dialog);

		editTextUriFromUser = (EditText) findViewById(R.id.editTextUriFromUser);
		buttonAddUriFromUser = (Button) findViewById(R.id.buttonAddUriFromUserAdd);
		buttonCancel = (Button) findViewById(R.id.buttonAddUriFromUserCancel);

		buttonAddUriFromUser.setOnClickListener(this);
		buttonCancel.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_uri_dialog, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// Put text from user in activity result and close this activity
		if (v == buttonAddUriFromUser) {

			String uriFromUser = editTextUriFromUser.getText().toString();

			Intent resultIntent = new Intent();
			resultIntent.putExtra(
					getResources().getString(R.string.URI_FROM_USER),
					uriFromUser);
			setResult(Activity.RESULT_OK, resultIntent);

			finish();
		}

		if (v == buttonCancel) {
			finish();
		}
	}

}
