package com.example.nfc_from_scratch;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.app.Activity;
import android.nfc.NdefRecord;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class TextRecord  implements ParsedNdefRecord{
	/** ISO/IANA language code */
    private final String mLanguageCode;

    private final String mText;

    private TextRecord(String languageCode, String text) {
        mLanguageCode = Preconditions.checkNotNull(languageCode);
        mText = Preconditions.checkNotNull(text);
    }

    public View getView(Activity activity, LayoutInflater inflater, ViewGroup parent, int offset) {
        TextView text = (TextView) inflater.inflate(R.layout.tag_text, parent, false);
        text.setText(mText);
        return text;
    }

    public String getText() {
        return mText;
    }

    /**
     * Returns the ISO/IANA language code associated with this text element.
     */
    public String getLanguageCode() {
        return mLanguageCode;
    }

    // TODO: deal with text fields which span multiple NdefRecords
    public static TextRecord parse(NdefRecord record) {
        Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));
        try {
            byte[] payload = record.getPayload();
            /*
             * payload[0] contains the "Status Byte Encodings" field, per the
             * NFC Forum "Text Record Type Definition" section 3.2.1.
             *
             * bit7 is the Text Encoding Field.
             *
             * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
             * The text is encoded in UTF16
             *
             * Bit_6 is reserved for future use and must be set to zero.
             *
             * Bits 5 to 0 are the length of the IANA language code.
             */
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            String text =
                new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return new TextRecord(languageCode, text);
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isText(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Create a new textRecord with UTF-8 encoding.
     * 
     * @param textToBeStored 
     * @return record to be written on NFC Tag
     */
    public static NdefRecord createNewTextRecord(String textToBeStored)
    {

    	try {
    		byte[] payload = textToBeStored.getBytes(Charset.forName("US-ASCII"));
    		String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
    		int languageCodeLength = payload[0] & 0077;
    		String languageCode = new String(payload, 1, languageCodeLength,
    				"US-ASCII");
    		String text = new String(payload, languageCodeLength + 1,
    				payload.length - languageCodeLength - 1, textEncoding);

        	// record to launch Play Store if app is not installed
            // record that contains our custom "retro console" game data, using custom MIME_TYPE
            String mimeType = "text/nfc-service-tag";
            //byte[] payload = "textToBeWritten".getBytes(Charset.forName("US-ASCII"));
            byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, 
            		NdefRecord.RTD_TEXT,
                    new byte[0], 
                    text.getBytes());//new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
            return record;
		} catch (Exception e) {
			return null;
		}
    	
    }
}
