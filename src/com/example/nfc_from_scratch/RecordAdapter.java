package com.example.nfc_from_scratch;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.NdefRecord;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class RecordAdapter extends BaseAdapter implements OnClickListener {

	private Context context;

	private List<NdefRecord> listOfRecords;

	public RecordAdapter(Context context, List<NdefRecord> listOfMessages) {
		this.context = context;
		this.listOfRecords = listOfMessages;
	}

	@Override
	public int getCount() {
		return listOfRecords.size();
	}

	@Override
	public Object getItem(int index) {

		return listOfRecords.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		NdefRecord entry = listOfRecords.get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater
					.inflate(R.layout.message_custom_layout, null);
		}

		TextView tvMainText = (TextView) convertView
				.findViewById(R.id.textViewMainItem);
		tvMainText.setText("Type: " + getTypeInStringFormat(entry));

		TextView tvDetailText = (TextView) convertView
				.findViewById(R.id.textViewDetailItem);
		tvDetailText.setText("Id: " + entry.getId());

		return convertView;
	}

	private String getTypeInStringFormat(NdefRecord entry) {
		String type = "Undefined";
		if (TextRecord.isText(entry)) {
			type = "Text";
		}
		if (UriRecord.isUri(entry)) {
			type = "Uri";
		}
		if (SmartPoster.isPoster(entry)) {
			type = "Smart Poster";
		}
		return type;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

}
