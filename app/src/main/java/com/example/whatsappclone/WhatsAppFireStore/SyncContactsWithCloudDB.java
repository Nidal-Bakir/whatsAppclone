package com.example.whatsappclone.WhatsAppFireStore;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;


import androidx.annotation.NonNull;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.example.whatsappclone.WhatsApp_Models.StatusPrivacyModel;
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
    private List<DataBase.Contact> contacts;
    private OnSyncFinish onSyncFinish;
    private List<DataBase.Contact_Profile> contact_profiles = null;
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
    protected Void doInBackground(Boolean... booleans) {
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
        DataBase dataBase = new DataBase(context);

        if (booleans[0]) {
            //read all contacts in phone and sync them
            //and add the new contacts to data base if there is any **
            // and Check the compatibility between contacts names in Db and user contacts names
            force_syncContacts(contacts);
            //for notify the adapter
            contact_profiles = dataBase.getAllContact();
        } else {
            //if the Contacts_Table is Empty the contacts will sync
            // and Check the compatibility between contacts names in Db and user contacts names
            if (dataBase.isContactTableEmpty()) {
                force_syncContacts(contacts);
            } else syncContacts(contacts);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        onSyncFinish.onFinish(contact_profiles);
    }

    private void syncContacts(List<DataBase.Contact> contacts) {
        final DataBase dataBase = new DataBase(context);
        for (DataBase.Contact contact : contacts) {
            if (dataBase.getContact(null, contact.getPhone_number()) != null) {
                dataBase.updateContact(contact.getContact_name(), null, contact.getPhone_number());
                dataBase.upDateContactNameInStatusPrivacy(contact.getContact_name(), null, contact.getPhone_number());
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
            while (cursor.moveToNext()) {
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
                        ArrayOfContacts.add(new DataBase.Contact(null, phoneNumber, name, null));
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
                    contacts1.add(new DataBase.Contact(null, newNumber, contact.getContact_name(), null));
                }

        }

        return contacts1;
    }

    private void force_syncContacts(List<DataBase.Contact> contacts) {
        final DataBase dataBase = new DataBase(context);
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        CollectionReference profilesReference = fireStore.collection("profile");
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
                        contact.setOnline_status(profile.getOnLineStatus());

                        //if the data exist just upDate the data else add the data to the tables
                        if (dataBase.getContact(profile.getUid(), null) == null) {
                            //add the contact to contacts table
                            dataBase.addContact(contact);
                            //add the profile image to the profile table and status image to status table
                            ProfileImage profileImage = profile.getProfileImage();
                            dataBase.insertUserProfile(profile.getUid()
                                    , profile.getPhoneNumber()
                                    , profileImage);
                            dataBase.addStatusImage(profile.getUid(), profile.getPhoneNumber());
                            //add the contact to the status privacy table
                            dataBase.addContactToStatusPrivacy(
                                    new StatusPrivacyModel(contact.getUID()
                                            , contact.getPhone_number()
                                            , contact.getContact_name()
                                            , true));// true the default so anyone can see your status

                        } else {
                            //update contact name and online Status
                            dataBase.updateContact(contact.getContact_name(), profile.getUid(), null);
                            dataBase.upDateOnlineStatusForUser(profile.getUid(), null, profile.getOnLineStatus());
                            //update profile and status image
                            dataBase.upDateProfileImage(profile.getUid(), profile.getProfileImage());
                            //update contact name in status privacy table
                            dataBase.upDateContactNameInStatusPrivacy(contact.getContact_name(), profile.getUid(), null);
                        }


                    }
                }


            });

        }
    }

    public void setOnSyncFinish(OnSyncFinish onSyncFinish) {
        this.onSyncFinish = onSyncFinish;
    }

    public interface OnSyncFinish {
        void onFinish(List<DataBase.Contact_Profile> contact_profiles);
    }
}

