package com.example.nfc_from_scratch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener,
		OnItemClickListener {

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private NdefMessage mNdefPushMessage;
	private IntentFilter[] mWriteTagFilters;
	

	// ListViewers and related objetc
	MessageAdapter listViewAdapterMessage;
	ListView listViewMessages;
	RecordAdapter listViewAdapterRecords;
	ListView listViewRecords;
	LinearLayout linearLayoutMessage;
	LinearLayout linearLayoutRecord;
	
	private ArrayList<NdefMessage> listOfMessages;
	private ArrayList<NdefRecord> listOfRecords;

	EditText editTextTipo;
	Button buttonAdicionarTag;

	// True: escreve na tag
	// False: apenas l� o conte�do
	Boolean gravarTag = false;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		inicializeGUIComponents();

		mAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mAdapter == null) {
			// showMessage(R.string.error, R.string.no_nfc);
			printToast("Error while instantiate mAdapter");
			finish();
			return;
		}

		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		mNdefPushMessage = new NdefMessage(new NdefRecord[] { newTextRecord(
				"Message from NFC Reader :-)", Locale.ENGLISH, true) });
	}

	private void inicializeGUIComponents() {

		// Single components
		editTextTipo = (EditText) findViewById(R.id.editTextTipo);
		buttonAdicionarTag = (Button) findViewById(R.id.buttonAdicionarTag);
		buttonAdicionarTag.setOnClickListener(this);

		linearLayoutMessage = (LinearLayout)findViewById(R.id.linearLayoutListOfMessages);
		linearLayoutRecord = (LinearLayout)findViewById(R.id.linearLayoutListOfRecords);
		
		// ListViewers
		listViewMessages = (ListView) findViewById(R.id.listViewMessages);
		listViewMessages.setClickable(true);
		listViewMessages.setOnItemClickListener(this);
		
		listViewRecords = (ListView) findViewById(R.id.listViewRecords);
		listViewRecords.setClickable(true);
		listViewRecords.setOnItemClickListener(this);
		
		//android.view.View.VISIBLE
		//android.view.View.android.view.View.INVISIBLE
		//android.view.View.GONE
		//listViewRecords.setVisibility(android.view.View.v);

		// First listView just for testing :p
		listOfMessages = new ArrayList<NdefMessage>();
		listOfRecords = new ArrayList<NdefRecord>();
		
		try {

			NdefRecord r0 = NdefRecord.createUri("www.google.com");
			NdefRecord r1 = NdefRecord.createUri("www.yahoo.com");
			NdefRecord records[] = new NdefRecord[2];
			records[0] = r0;
			records[1] = r1;
			NdefMessage testNdef = new NdefMessage(records);

			// NdefMessage message = new NdefMessage(records);
			listOfMessages.add(testNdef);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		listViewAdapterMessage = new MessageAdapter(this, listOfMessages);
		listViewAdapterRecords = new RecordAdapter(this,listOfRecords);

		listViewMessages.setAdapter(listViewAdapterMessage);
		listViewRecords.setAdapter(listViewAdapterRecords);

	}

	private NdefRecord newTextRecord(String text, Locale locale,
			boolean encodeInUtf8) {
		byte[] langBytes = locale.getLanguage().getBytes(
				Charset.forName("US-ASCII"));

		Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
				.forName("UTF-16");
		byte[] textBytes = text.getBytes(utfEncoding);

		int utfBit = encodeInUtf8 ? 0 : (1 << 7);
		char status = (char) (utfBit + langBytes.length);

		byte[] data = new byte[1 + langBytes.length + textBytes.length];
		data[0] = (byte) status;
		System.arraycopy(langBytes, 0, data, 1, langBytes.length);
		System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
				textBytes.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
				new byte[0], data);
	}

	private void printToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mAdapter != null) {
			if (!mAdapter.isEnabled()) {
				printToast("Please turn on the NFC!");
			}

			// Here the listening for discovery tag is set.
			mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
			mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
		}
		if (listViewAdapterMessage != null) {
			// messageAdapter.notify();
			// messageAdapter.notifyAll();
			listViewAdapterMessage.notifyDataSetChanged();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
			mAdapter.disableForegroundNdefPush(this);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		printToast("New intent");
		resolveIntent(intent);

	}

	private void resolveIntent(Intent intent) {

		if (gravarTag) {
			// GRAVA TAG
			String action = intent.getAction();
			if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
					|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
					|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
				printToast("tag or tech or ndef discovered");

				Tag detectedTag = intent
						.getParcelableExtra(NfcAdapter.EXTRA_TAG);

				Parcelable[] rawMsgs = intent
						.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				NdefMessage[] msgs;
				if (rawMsgs != null) {
					msgs = new NdefMessage[rawMsgs.length];
					for (int i = 0; i < rawMsgs.length; i++) {
						msgs[i] = (NdefMessage) rawMsgs[i];
						try {
							setTipo(new String(
									msgs[0].getRecords()[0].getPayload(),
									"UTF-8"));
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							setTipo(e.getMessage());
							e.printStackTrace();
						}
					}
					writeTag(msgs[0], detectedTag);
					// writeTag(detectedTag);
					/*
					 * NdefRecord record = NdefRecord.createUri("www.bing.com");
					 * addRecordToTag(detectedTag, msgs[0], record);
					 */
				}

			} else {
				printToast("Not tag or tech or ndef discovered");
				// Unknown tag type

			}
		} else {
			// LÊ TAG
			String action = intent.getAction();
			if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
					|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
					|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
				printToast("tag or tech or ndef discovered");

				Parcelable[] rawMsgs = intent
						.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				NdefMessage[] messages;
				if (rawMsgs != null) {

					messages = new NdefMessage[rawMsgs.length];

					// clear listOfMessages
					listOfMessages.clear();
					// put every message in listOfMessages
					for (int i = 0; i < rawMsgs.length; i++) {
						listOfMessages.add((NdefMessage) rawMsgs[i]);

					}

					// messageAdapter.notify();
					// messageAdapter.notifyAll();
					listViewAdapterMessage.notifyDataSetChanged();

					// For every rawMsg get NdefMessage[]
					for (int i = 0; i < rawMsgs.length; i++) {
						messages[i] = (NdefMessage) rawMsgs[i];

						Log.d("LER_TAG", "message " + i);
						for (int j = 0; j < messages[i].getRecords().length; j++) {

							try {
								setTipo(new String(
										messages[i].getRecords()[j]
												.getPayload(),
										"UTF-8"));

								Log.d("LER_TAG", "record " + j);
								Log.d("LER_TAG",
										new String(messages[i].getRecords()[j]
												.getPayload(), "UTF-8"));
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								setTipo(e.getMessage());
								e.printStackTrace();
							}
						}

					}

				}

			} else {
				printToast("Not tag or tech or ndef discovered");
				// Unknown tag type

			}
		}

		// "RESETA variável para não gravar sempre
		gravarTag = false;

	}

	private void setTipo(String tipo) {
		editTextTipo.setText(tipo);
	}

	private boolean writableTag(final Tag tag) {

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					Toast.makeText(getApplicationContext(),
							"Tag is read-only.", Toast.LENGTH_SHORT).show();
					ndef.close();
					return false;
				}
				ndef.close();
				return true;
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Failed to read tag",
					Toast.LENGTH_SHORT).show();
		}

		return false;
	}

	/**
	 * Add a record to a TAG.
	 * 
	 * TODO: see if there is size available for new record
	 * 
	 * @param tag
	 *            NFC Tag where data should be stored
	 * @param message
	 *            Message that contains array of records to be written.
	 * @param record
	 *            New record to be add to this message
	 * @return TRUE if record could be written and FALSE if could not be
	 *         written.
	 */
	public Boolean addRecordToTag(Tag tag, NdefMessage message,
			NdefRecord record) {

		NdefRecord[] currentRecords, newRecords;
		int newNumberOfRecords;

		currentRecords = message.getRecords();
		newNumberOfRecords = currentRecords.length + 1;
		newRecords = new NdefRecord[newNumberOfRecords];

		for (int i = 0; i < currentRecords.length; i++) {
			newRecords[i] = currentRecords[i];
		}
		newRecords[newNumberOfRecords - 1] = record;

		try {
			Ndef ndef = Ndef.get(tag);

			if (ndef != null) {
				ndef.connect();
				NdefMessage newmessage = new NdefMessage(newRecords);
				ndef.writeNdefMessage(newmessage);
				printToast("Tag have been written");
				return true;
			}

		} catch (Exception e) {
			printToast("Fail to write in Tag");
			return false;
		}

		return false;
	}

	public Boolean writeTag(Tag tag) {
		try {
			Ndef ndef = Ndef.get(tag);

			if (ndef != null) {
				ndef.connect();
				NdefRecord r0 = NdefRecord.createUri("www.duck.com");
				// NdefRecord r1 = NdefRecord.createUri("www.google.com");
				NdefRecord records[] = new NdefRecord[1];
				records[0] = r0;
				// records[1] = r1;
				NdefMessage testNdef = new NdefMessage(records);
				ndef.writeNdefMessage(testNdef);
				printToast("Tag have been written");
				return true;
			}

		} catch (Exception e) {
			printToast("Fail to write in Tag");
			return false;
		}

		return false;
	}

	public WriteResponse writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		String mess = "";

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();

				if (!ndef.isWritable()) {
					return new WriteResponse(0, "Tag is read-only");

				}
				if (ndef.getMaxSize() < size) {
					mess = "Tag capacity is " + ndef.getMaxSize()
							+ " bytes, message is " + size + " bytes.";
					return new WriteResponse(0, mess);
				}

				NdefRecord r0 = NdefRecord.createUri("www.google.com");
				NdefRecord r1 = NdefRecord.createUri("www.yahoo.com");
				NdefRecord records[] = new NdefRecord[2];
				records[0] = r0;
				records[1] = r1;
				NdefMessage testNdef = new NdefMessage(records);
				ndef.writeNdefMessage(testNdef);
				// ndef.writeNdefMessage(message);
				/*
				 * if(writeProtect) ndef.makeReadOnly(); mess =
				 * "Wrote message to pre-formatted tag.";
				 */
				return new WriteResponse(1, mess);
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						mess = "Formatted tag and wrote message";
						return new WriteResponse(1, mess);
					} catch (IOException e) {
						mess = "Failed to format tag.";
						return new WriteResponse(0, mess);
					}
				} else {
					mess = "Tag doesn't support NDEF.";
					return new WriteResponse(0, mess);
				}
			}
		} catch (Exception e) {
			mess = "Failed to write tag";
			return new WriteResponse(0, mess);
		}
	}

	private class WriteResponse {
		int status;
		String message;

		WriteResponse(int Status, String Message) {
			this.status = Status;
			this.message = Message;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
	}

	@Override
	public void onClick(View v) {

		if (v == buttonAdicionarTag) {
			// GravaTag
			gravarTag = true;
			this.printToast("Coloque o aparelho perto da TAG NFC!");
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long id){
		//Hide(gone) listViewMessages
		linearLayoutMessage.setVisibility(android.view.View.GONE);
		//Show listViewRecords of index index(parameter)
		linearLayoutRecord.setVisibility(android.view.View.VISIBLE);
		
		for (int i = 0; i < listOfMessages.get(index).getRecords().length; i++) {
			listOfRecords.add(listOfMessages.get(index).getRecords()[i]);
		}
		listViewAdapterRecords.notifyDataSetChanged();

	}
}
