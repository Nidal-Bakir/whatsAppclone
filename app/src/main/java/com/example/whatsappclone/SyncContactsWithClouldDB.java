package com.example.whatsappclone;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SyncContactsWithClouldDB extends AsyncTask<Boolean, Void, Void> {
    private Context context;
    private String countryCode;
    private static final String TAG = "SyncContactsWithClouldD";

    /**
     * this class is the responsible for download the profile images
     * and the Status image
     **/
    public SyncContactsWithClouldDB(Context context,String countryCode) {
        this.countryCode=countryCode;
        this.context = context;

    }


    @Override
    protected void onPreExecute() {
        //read the contacts from phone
        getContactsFromUserPhone();

    }

    @Override
    protected Void doInBackground(Boolean... booleans) {
        if (booleans[0]) {
            //read all contacts in phone and sync them
            //and add the new contacts to data base if there is any **
            // and Check the compatibility between contacts names in Db and user contacts names
        } else {
            //if the Contacts_Table is Empty the contacts will sync
            // and Check the compatibility between contacts names in Db and user contacts names
        }
        return null;
    }

    private void getContactsFromUserPhone() {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            while (cursor != null && cursor.moveToNext()) {
                String id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

    }

}
