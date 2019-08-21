package com.example.whatsappclone.WhatsAppFireStore;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;


import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

public class SyncContactsWithCloudDB extends AsyncTask<Boolean, Void, Void> {
    private static final String TAG = "SyncContactsWithCloudDB";
    private Context context;
    private String countryCode;
    //i.g US this will use with libphonenumber lib
    // to handle the numbers whose  doesn't have area code i.g(+1)


    /**
     * this class is the responsible for download the profile images
     * and the Status image
     **/
    public SyncContactsWithCloudDB(Context context, String countryCode) {
        this.countryCode = countryCode;
        this.context = context;

    }


    @Override
    protected void onPreExecute() {
        //read the contacts from phone
        List<DataBase.Contact> contacts = getContactsFromUserPhone();
        //filter the contacts (get Valid contacts) and add area code
        if (contacts != null) {
            try {
                contacts = ScanContacts(contacts);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
        }

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

    //get all contacts from user phone
    private List getContactsFromUserPhone() {
        // @DataBase.Contact
        List<DataBase.Contact> ArrayOfContacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI
                , null, null, null, null);
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
                    if (pCur.moveToFirst()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        ArrayOfContacts.add(new DataBase.Contact(null, phoneNumber, name));
                    }

                    pCur.close();
                }
            }
            return ArrayOfContacts;
        } else {//there is no contacts in user phone
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }

    }

    private List ScanContacts(List<DataBase.Contact> contacts) throws NumberParseException {
        List<DataBase.Contact>contacts1=new ArrayList<>();
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        //loop throw all contacts
        for (DataBase.Contact contact : contacts) {
            //add countryCode number don't have
            Phonenumber.PhoneNumber phoneNumber = numberUtil.parse(contact.getPhone_number(), countryCode.toUpperCase());
            if (numberUtil.isPossibleNumber(phoneNumber))
                if (numberUtil.isValidNumber(phoneNumber)) {
                  contacts1.add(contact);
                }

        }
        return null;
    }
}
