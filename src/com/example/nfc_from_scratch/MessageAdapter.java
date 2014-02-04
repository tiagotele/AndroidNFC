package com.example.nfc_from_scratch;

import java.util.List;



import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.NdefMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MessageAdapter extends BaseAdapter implements OnClickListener {

	
	private Context context;

    private List<NdefMessage> listOfMessages;

    public MessageAdapter(Context context, List<NdefMessage> listOfMessages) {
        this.context = context;
        this.listOfMessages = listOfMessages;
    }
	
	@Override
	public int getCount() {
		return listOfMessages.size();
	}

	@Override
	public Object getItem(int index) {
		
		return listOfMessages.get(index);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		NdefMessage entry = listOfMessages.get(position);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.message_custom_layout, null);
        }
        TextView tvMainText = (TextView) convertView.findViewById(R.id.textViewMainItem);
        tvMainText.setText(context.getResources().getString(R.string.custom_layout_text_Size)+entry.getByteArrayLength());

        TextView tvDetailText = (TextView) convertView.findViewById(R.id.textViewDetailItem);
        tvDetailText.setText(context.getResources().getString(R.string.custom_layout_number_of_records)+entry.getRecords().length);

        return convertView;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}

}
