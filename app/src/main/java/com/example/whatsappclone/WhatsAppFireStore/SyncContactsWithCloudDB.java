package com.example.whatsappclone.WhatsAppFireStore;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;


import androidx.annotation.NonNull;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.Profile_Status_img;
import com.example.whatsappclone.WhatsApp_Models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

public class SyncContactsWithCloudDB extends AsyncTask<Boolean, Void, Void> {
    private static final String TAG = "SyncContactsWithCloudDB";
    private Context context;
    private String countryCode;
    List<DataBase.Contact> contacts;
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
        contacts = getContactsFromUserPhone();
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
        DataBase dataBase = new DataBase(context);

        if (booleans[0]) {
            //read all contacts in phone and sync them
            //and add the new contacts to data base if there is any **
            // and Check the compatibility between contacts names in Db and user contacts names
            force_syncContacts(contacts);
        } else {
            //if the Contacts_Table is Empty the contacts will sync
            // and Check the compatibility between contacts names in Db and user contacts names
            if (dataBase.isContactTableEmpty()) {
                force_syncContacts(contacts);
            } else syncContacts(contacts);
        }
        return null;
    }

    private void syncContacts(List<DataBase.Contact> contacts) {
        final DataBase dataBase = new DataBase(context);
        for (DataBase.Contact contact : contacts) {
            if (dataBase.getContact(null, contact.getPhone_number()) != null) {
                dataBase.updateContact(contact.getContact_name(), null, contact.getPhone_number());
            }
        }
    }

    //get all contacts from user phone
    private List getContactsFromUserPhone() {
        // DataBase.Contact model to handel contact
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
        List<DataBase.Contact> contacts1 = new ArrayList<>();
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        //loop throw all contacts
        for (DataBase.Contact contact : contacts) {
            //add countryCode number don't have
            Phonenumber.PhoneNumber phoneNumber = numberUtil.parse(contact.getPhone_number(), countryCode.toUpperCase());
            String newNumber = numberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            if (numberUtil.isPossibleNumber(phoneNumber))
                if (numberUtil.isValidNumber(phoneNumber)) {
                    contacts1.add(new DataBase.Contact(null, newNumber, contact.getContact_name()));
                }

        }

        return contacts1;
    }

    private void force_syncContacts(List<DataBase.Contact> contacts) {
        final DataBase dataBase = new DataBase(context);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference profilesReference = firestore.collection("profile");
        for (final DataBase.Contact contact : contacts) {
            //add every contact to DB if they have an account
            profilesReference.whereEqualTo("phoneNumber", contact.getPhone_number())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot taskResult = task.getResult();
                    if (taskResult.isEmpty())
                        return; //the number Not Found(don't have account )
                    for (QueryDocumentSnapshot snapshot : taskResult) {
                        UserProfile profile = snapshot.toObject(UserProfile.class);
                        contact.setUID(profile.getUid());
                        //add the contact to contacts table
                        //if the data exist just upDate the data
                        if (dataBase.getContact(profile.getUid(), null) == null)
                            dataBase.addContact(contact);
                        else //update
                            dataBase.updateContact(contact.getContact_name(), profile.getUid(), null);
                        Profile_Status_img profile_status = new Profile_Status_img(
                                profile.getProfileImage()
                                , profile.getStatus());
                        //add the profile image and status to the profile_status table
                            //if the data exist just upDate the data
                        if (dataBase.getUserProfileAndStatus(profile.getUid()) == null)
                            dataBase.insetUserProfileAndStatus(profile.getUid()
                                    , profile.getPhoneNumber()
                                    , profile_status);
                        else {//update
                            dataBase.upDateProfileImage(profile.getUid(), profile.getProfileImage());
                            dataBase.upDateSatusImage(profile.getUid(), profile.getStatus());
                        }
                    }
                }


            });

        }
    }
}
