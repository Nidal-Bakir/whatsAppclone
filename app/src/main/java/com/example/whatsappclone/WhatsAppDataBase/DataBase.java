package com.example.whatsappclone.WhatsAppDataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.GenralContact;
import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.example.whatsappclone.WhatsApp_Models.Profile_Status_img;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.StatusPrivacyModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {
    private static final String TAG = "DataBase";
    private static final int EMPTYCURSOR = 0;
    private static final String DB_NAME = "whatsApp_DB";
    private static final long MILLI_SECOND_DAY = 86400000;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference statusCollectionRef =
            firestore.collection("profile").document(UserSettings.PHONENUMBER).collection("status");
    private SQLiteDatabase database;

    /* Inner class that defines the ProfilesTable table contents  */
    private class ProfilesTable implements BaseColumns {
        private static final String TABLE_NAME = "Profiles";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String IMAGE_PATH = "imagePath";
        private static final String IMAGE_URL = "imageUrl";
    }

    private static final String SQL_CREATE_PROFILES_IMG =
            "CREATE TABLE " + ProfilesTable.TABLE_NAME
                    + " ("
                    + ProfilesTable.UID + " TEXT,"
                    + ProfilesTable.PHONE_NUMBER + " TEXT ,"
                    + ProfilesTable.IMAGE_PATH + " TEXT,"
                    + ProfilesTable.IMAGE_URL + " TEXT "
                    + ")";

    /* Inner class that defines the StatusTable table contents  */
    private class StatusTable implements BaseColumns {
        private static final String TABLE_NAME = "Status";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String STATUS_PATH = "statusPath";
        private static final String STATUS_URL = "statusUrl";
        private static final String DATE = "date";
    }

    private static final String SQL_CREATE_STATUS_IMG =
            "CREATE TABLE " + StatusTable.TABLE_NAME
                    + " ("
                    + StatusTable.UID + " TEXT,"
                    + StatusTable.PHONE_NUMBER + " TEXT ,"
                    + StatusTable.STATUS_PATH + " TEXT,"
                    + StatusTable.STATUS_URL + " TEXT ,"
                    + StatusTable.DATE + " TEXT"
                    + ")";

    /* Inner class that defines the ContactsTable table contents  */
    private class ContactsTable implements BaseColumns {
        private static final String TABLE_NAME = "contacts";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String CONTACT_NAME = "contact_name";
        private static final String ONLINE_STATUS = "online_status";
    }


    //this class for handel the contact result
    public static class Contact extends GenralContact {
        private String online_status;

        public Contact(String UID, String phone_number, String contact_name, String online_status) {
            super(UID, phone_number, contact_name);
            this.online_status = online_status;
        }

        public String getOnline_status() {
            return online_status;
        }

        public void setOnline_status(String online_status) {
            this.online_status = online_status;
        }
    }

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + ContactsTable.TABLE_NAME
                    + " ("
                    + ContactsTable.UID + " TEXT,"
                    + ContactsTable.PHONE_NUMBER + " TEXT,"
                    + ContactsTable.CONTACT_NAME + " TEXT ,"
                    + ContactsTable.ONLINE_STATUS + " TEXT"
                    + ")";

    //class for handel the contact and profile image
    public static class Contact_Profile {
        private ProfileImage profileImage;
        private Contact contact;

        public Contact_Profile(ProfileImage profileImage, Contact contact) {
            this.profileImage = profileImage;
            this.contact = contact;
        }

        public ProfileImage getProfileImage() {
            return profileImage;
        }

        public Contact getContact() {
            return contact;
        }
    }

    private class PrivacyTable implements BaseColumns {
        private static final String TABLE_NAME = "statusPrivacy";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String CONTACT_NAME = "contact_name";
        private static final String AUTHORIZED = "authorized";
    }

    private static final String SQL_CREATE_STATUS_PRIVACY_TABLE =
            "CREATE TABLE " + PrivacyTable.TABLE_NAME
                    + " ("
                    + PrivacyTable.UID + " TEXT,"
                    + PrivacyTable.PHONE_NUMBER + " TEXT,"
                    + PrivacyTable.CONTACT_NAME + " TEXT ,"
                    + PrivacyTable.AUTHORIZED + " INTEGER" // for true or false (1 OR 0)
                    + ")";

    public DataBase(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILES_IMG);
        db.execSQL(SQL_CREATE_STATUS_IMG);
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);
        db.execSQL(SQL_CREATE_STATUS_PRIVACY_TABLE);
        //add user info to database the def info
        setDefaultUserInfo(new Contact(UserSettings.UID, UserSettings.PHONENUMBER, "Me", "online"), db);
        Profile_Status_img profileStatusImg = new Profile_Status_img(new ProfileImage(null, null)
                , new Status(null, null, null));
        setDefaultUserInfo(UserSettings.UID, UserSettings.PHONENUMBER, profileStatusImg, db);
        setDefaultUserInfo(new StatusPrivacyModel(UserSettings.UID, UserSettings.PHONENUMBER, "Me", true), db);
    }

    /**
     * use this tow methods to create the def info for user when hte app
     * launch for the first time (and if i tired to call getRead or write DB)
     * that will throw IllegalStateException: getDatabase called recursively so i use db parameter
     **/
    private void setDefaultUserInfo(Contact contact, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsTable.UID, contact.getUID());
        contentValues.put(ContactsTable.PHONE_NUMBER, contact.getPhone_number());
        contentValues.put(ContactsTable.CONTACT_NAME, contact.getContact_name());
        contentValues.put(ContactsTable.ONLINE_STATUS, contact.getOnline_status());
        db.insert(
                ContactsTable.TABLE_NAME
                , null
                , contentValues);

    }

    // her is the same
    private void setDefaultUserInfo(String UID, String phoneNumber, Profile_Status_img profile_status_img, SQLiteDatabase db) {
        ContentValues profileContentValues = new ContentValues();
        ContentValues statusContentValues = new ContentValues();
        //for profile table
        profileContentValues.put(ProfilesTable.UID, UID);
        profileContentValues.put(ProfilesTable.PHONE_NUMBER, phoneNumber);
        //for status table
        statusContentValues.put(StatusTable.UID, UID);
        statusContentValues.put(StatusTable.PHONE_NUMBER, phoneNumber);
        //handel if the profile don't have profile image
        if (profile_status_img.getProfileImage() != null) {
            profileContentValues.put(ProfilesTable.IMAGE_PATH, profile_status_img.getProfilePath());
            profileContentValues.put(ProfilesTable.IMAGE_URL, profile_status_img.getProfileUrl());
        }
        //handel if the profile don't status
        if (profile_status_img.getStatus() != null) {
            statusContentValues.put(StatusTable.STATUS_PATH, profile_status_img.getStatusPath());
            statusContentValues.put(StatusTable.STATUS_URL, profile_status_img.getStatusUrl());
            statusContentValues.put(StatusTable.DATE, profile_status_img.getStatusDate());
        }
        db.insert(ProfilesTable.TABLE_NAME, null, profileContentValues);
        db.insert(StatusTable.TABLE_NAME, null, statusContentValues);


    }

    private void setDefaultUserInfo(StatusPrivacyModel statusPrivacyModel, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivacyTable.UID, statusPrivacyModel.getUID());
        contentValues.put(PrivacyTable.PHONE_NUMBER, statusPrivacyModel.getPhone_number());
        contentValues.put(PrivacyTable.CONTACT_NAME, statusPrivacyModel.getContact_name());
        contentValues.put(PrivacyTable.AUTHORIZED, statusPrivacyModel.isAuthorized());
        db.insert(
                PrivacyTable.TABLE_NAME
                , null
                , contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TOdo:get the UIDs of tables to delete them
//        db.execSQL("DELETE ");
//        onCreate(db);
    }

    //this function will get the image for profile
    public ProfileImage getUserProfile(String UID) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(ProfilesTable.TABLE_NAME
                , null
                , ProfilesTable.UID + " = ? "
                , new String[]{UID}
                , null
                , null
                , null);
        if (cursor.getCount() == EMPTYCURSOR)
            return null;
        cursor.moveToFirst();  //move to the element
        String imagePath = cursor.getString(cursor.getColumnIndex(ProfilesTable.IMAGE_PATH));
        String imageUrl = cursor.getString(cursor.getColumnIndex(ProfilesTable.IMAGE_URL));
        cursor.close();
        return new ProfileImage(imagePath, imageUrl);
    }

    public Status getUserStatus(String UID) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(StatusTable.TABLE_NAME
                , null
                , ProfilesTable.UID + " = ? "
                , new String[]{UID}
                , null
                , null
                , null);
        if (cursor.getCount() == EMPTYCURSOR)
            return null;
        cursor.moveToFirst();  //move to the element
        String statusPath = cursor.getString(cursor.getColumnIndex(StatusTable.STATUS_PATH));
        String statusUrl = cursor.getString(cursor.getColumnIndex(StatusTable.STATUS_URL));
        String date = cursor.getString(cursor.getColumnIndex(StatusTable.DATE));
        cursor.close();
        return new Status(statusPath, statusUrl, date);
    }

    public List<Status> getAllStatus() {
        Calendar calendar = Calendar.getInstance();
        List<Status> statusList = new ArrayList<>();
        database = this.getReadableDatabase();
        Cursor cursor = database.query(StatusTable.TABLE_NAME
                , null
                , StatusTable.STATUS_URL + " != ?"
                , new String[]{""}
                , null
                , null
                , StatusTable.DATE + " ASC");
        //check if the cursor is not null
        if (cursor.getCount() != EMPTYCURSOR)
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(StatusTable.STATUS_PATH));
                String url = cursor.getString(cursor.getColumnIndex(StatusTable.STATUS_URL));
                String date = cursor.getString(cursor.getColumnIndex(StatusTable.DATE));
                String phone_number = cursor.getString(cursor.getColumnIndex(StatusTable.PHONE_NUMBER));
                long longdate = Long.parseLong(date);
                // check if the status EXP or not
                // if true delete the status from status collection on fireStore
                if (calendar.getTimeInMillis() - longdate <= MILLI_SECOND_DAY) {
                    Status status = new Status(path, url, date);
                    status.setPhone_number(phone_number);
                    if (phone_number.equals(UserSettings.PHONENUMBER))// if the status is my status
                        statusList.add(0, status); //so my status will be the first item
                    else
                        statusList.add(status);
                } else {
                    statusCollectionRef.document(phone_number).delete();
                }
            }
        cursor.close();
        return statusList;
    }

    public boolean isNumberAFriend(String phone_number) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(ContactsTable.TABLE_NAME
                , null
                , ContactsTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number}
                , null
                , null
                , null);
        if (cursor.getCount() == EMPTYCURSOR) {
            cursor.close();
            return false;
        }
        // so the number saved in user contact and he know the number
        cursor.close();
        return true;
    }

    public void insetUserProfileAndStatus(String UID, String phoneNumber, Profile_Status_img profile_status_img) {
        database = this.getWritableDatabase();
        ContentValues profileContentValues = new ContentValues();
        ContentValues statusContentValues = new ContentValues();
        //for profile table
        profileContentValues.put(ProfilesTable.UID, UID);
        profileContentValues.put(ProfilesTable.PHONE_NUMBER, phoneNumber);
        //for status table
        statusContentValues.put(StatusTable.UID, UID);
        statusContentValues.put(StatusTable.PHONE_NUMBER, phoneNumber);
        //handel if the profile don't have profile image
        if (profile_status_img.getProfileImage() != null) {
            profileContentValues.put(ProfilesTable.IMAGE_PATH, profile_status_img.getProfilePath());
            profileContentValues.put(ProfilesTable.IMAGE_URL, profile_status_img.getProfileUrl());
        }
        //handel if the profile don't status
        if (profile_status_img.getStatus() != null) {
            statusContentValues.put(StatusTable.STATUS_PATH, profile_status_img.getStatusPath());
            statusContentValues.put(StatusTable.STATUS_URL, profile_status_img.getStatusUrl());
            statusContentValues.put(StatusTable.DATE, profile_status_img.getStatusDate());
        }
        database.insert(ProfilesTable.TABLE_NAME, null, profileContentValues);
        database.insert(StatusTable.TABLE_NAME, null, statusContentValues);


    }

    public void upDateProfileImage(String UID, ProfileImage profileImage) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (profileImage != null) {
            contentValues.put(ProfilesTable.IMAGE_PATH, profileImage.getImagePath());
            contentValues.put(ProfilesTable.IMAGE_URL, profileImage.getImageUrl());
            database.update(
                    ProfilesTable.TABLE_NAME
                    , contentValues
                    , ProfilesTable.UID + " = ?"
                    , new String[]{UID});
        }

    }

    public void upDateStatusImage(String UID, String phone_number, Status status) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if (status != null) {
            contentValues.put(StatusTable.STATUS_PATH, status.getStatusPath());
            contentValues.put(StatusTable.STATUS_URL, status.getStatusUrl());
            contentValues.put(StatusTable.DATE, status.getDate());
            if (UID != null)
                database.update(
                        StatusTable.TABLE_NAME
                        , contentValues
                        , StatusTable.UID + "= ?"
                        , new String[]{UID});
            else database.update(
                    StatusTable.TABLE_NAME
                    , contentValues
                    , StatusTable.PHONE_NUMBER + "= ?"
                    , new String[]{phone_number});
        }


    }

    public Contact getContact(String UID, String phone_number) {
        database = this.getReadableDatabase();
        Cursor cursor;
        //so i can search using UID OR phone number
        if (UID != null)
            cursor = database.query(
                    ContactsTable.TABLE_NAME
                    , null
                    , ContactsTable.UID + " = ?"
                    , new String[]{UID}
                    , null, null, null);
        else cursor = database.query(
                ContactsTable.TABLE_NAME
                , null
                , ContactsTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number}
                , null, null, null);
        if (cursor.getCount() != EMPTYCURSOR) {
            cursor.moveToFirst();
            return new Contact(
                    cursor.getString(cursor.getColumnIndex(ContactsTable.UID))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.PHONE_NUMBER))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.CONTACT_NAME))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.ONLINE_STATUS))
            );
        }
        cursor.close();
        return null;
    }

    //get all contacts Who own an account
    public List<Contact_Profile> getAllContact() {
        database = this.getReadableDatabase();
        List<Contact_Profile> contact_profiles = new ArrayList<>();
        Cursor cursor = database.query(ContactsTable.TABLE_NAME
                , null
                , null
                , null
                , null
                , null
                , ContactsTable.CONTACT_NAME);
        //loop throw all contact and get them
        while (cursor.moveToNext()) {
            Cursor ProfileCursor = database.query(ProfilesTable.TABLE_NAME
                    , new String[]{ProfilesTable.IMAGE_PATH, ProfilesTable.IMAGE_URL}
                    , ProfilesTable.UID + " = ?"
                    , new String[]{cursor.getString(cursor.getColumnIndex(ContactsTable.UID))}
                    , null
                    , null
                    , null);
            ProfileCursor.moveToFirst();
            //store the values
            ProfileImage profileImage = new ProfileImage(ProfileCursor.getString(ProfileCursor.getColumnIndex(ProfilesTable.IMAGE_PATH))
                    , ProfileCursor.getString(ProfileCursor.getColumnIndex(ProfilesTable.IMAGE_URL)));
            Contact contact = new Contact(cursor.getString(cursor.getColumnIndex(ContactsTable.UID))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.PHONE_NUMBER))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.CONTACT_NAME))
                    , cursor.getString(cursor.getColumnIndex(ContactsTable.ONLINE_STATUS)));
            //add to list
            contact_profiles.add(new Contact_Profile(profileImage, contact));
            ProfileCursor.close();
        }
        cursor.close();

        return contact_profiles;
    }

    public boolean isContactTableEmpty() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(ContactsTable.TABLE_NAME
                , null, null, null, null, null, null);
        boolean isEmpty = (cursor.getCount() - 1) == EMPTYCURSOR;
        cursor.close();
        return isEmpty;
    }

    public void addContact(Contact contact) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsTable.UID, contact.getUID());
        contentValues.put(ContactsTable.PHONE_NUMBER, contact.getPhone_number());
        contentValues.put(ContactsTable.CONTACT_NAME, contact.getContact_name());
        contentValues.put(ContactsTable.ONLINE_STATUS, contact.getOnline_status());
        database.insert(
                ContactsTable.TABLE_NAME
                , null
                , contentValues);

    }

    public void updateContact(String contactName, String UID, String phone_number) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsTable.CONTACT_NAME, contactName);
        //so i can update using UID OR phone number
        if (UID != null)
            database.update(
                    ContactsTable.TABLE_NAME
                    , contentValues
                    , ContactsTable.UID + " = ?"
                    , new String[]{UID});
        else database.update(
                ContactsTable.TABLE_NAME
                , contentValues
                , ContactsTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number});
    }

    public String getOnlineStateForUser(String uid, String phone_number) {
        Cursor cursor;
        //so i can search using UID OR phone number
        if (uid != null)
            cursor = database.query(
                    ContactsTable.TABLE_NAME
                    , null
                    , ContactsTable.UID + " = ?"
                    , new String[]{uid}
                    , null, null, null);
        else cursor = database.query(
                ContactsTable.TABLE_NAME
                , null
                , ContactsTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number}
                , null, null, null);
        if (cursor.getCount() == EMPTYCURSOR)
            return null;
        cursor.moveToFirst();
        String onlineState = cursor.getString(cursor.getColumnIndex(ContactsTable.ONLINE_STATUS));
        cursor.close();
        return onlineState;

    }

    public void upDateOnlineStatusForUser(String uid, String phone_number, String onlineStatus) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsTable.ONLINE_STATUS, onlineStatus);
        if (uid != null)
            database.update(ContactsTable.TABLE_NAME
                    , contentValues
                    , ContactsTable.UID + " =?"
                    , new String[]{uid});
        else database.update(ContactsTable.TABLE_NAME
                , contentValues
                , ContactsTable.PHONE_NUMBER + " =?"
                , new String[]{phone_number});
    }

    public void addContactToStatusPrivacy(StatusPrivacyModel statusPrivacyModel) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivacyTable.UID, statusPrivacyModel.getUID());
        contentValues.put(PrivacyTable.PHONE_NUMBER, statusPrivacyModel.getPhone_number());
        contentValues.put(PrivacyTable.CONTACT_NAME, statusPrivacyModel.getContact_name());
        contentValues.put(PrivacyTable.AUTHORIZED, statusPrivacyModel.isAuthorized());
        database.insert(
                PrivacyTable.TABLE_NAME
                , null
                , contentValues);
    }

    public void upDateContactNameInStatusPrivacy(String contactName, String UID, String phone_number) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivacyTable.CONTACT_NAME, contactName);
        //so i can update using UID OR phone number
        if (UID != null)
            database.update(
                    PrivacyTable.TABLE_NAME
                    , contentValues
                    , PrivacyTable.UID + " = ?"
                    , new String[]{UID});
        else database.update(
                PrivacyTable.TABLE_NAME
                , contentValues
                , PrivacyTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number});
    }

    public void upDateAuthorizedValue(int authorized, String UID, String phone_number) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PrivacyTable.AUTHORIZED, authorized);
        //so i can update using UID OR phone number
        if (UID != null)
            database.update(
                    PrivacyTable.TABLE_NAME
                    , contentValues
                    , PrivacyTable.UID + " = ?"
                    , new String[]{UID});
        else database.update(
                PrivacyTable.TABLE_NAME
                , contentValues
                , PrivacyTable.PHONE_NUMBER + " = ?"
                , new String[]{phone_number});
    }

    public List<StatusPrivacyModel> getAllContactsInStatusPrivacy() {
        database = this.getReadableDatabase();
        List<StatusPrivacyModel> privacyModelList = new ArrayList<>();
        boolean authorized;
        Cursor cursor = database.query(PrivacyTable.TABLE_NAME
                , null
                , StatusTable.UID + " != ?"
                , new String[]{UserSettings.UID}
                , null
                , null
                , null);
        while (cursor.moveToNext()) {
            //if the authorized filed == 1 then the contact authorized
            // else the contact not authorized
            authorized = cursor.getInt(cursor.getColumnIndex(PrivacyTable.AUTHORIZED)) == 1;
            StatusPrivacyModel statusPrivacyModel =
                    new StatusPrivacyModel(cursor.getString(cursor.getColumnIndex(PrivacyTable.UID))
                            , cursor.getString(cursor.getColumnIndex(PrivacyTable.PHONE_NUMBER))
                            , cursor.getString(cursor.getColumnIndex(PrivacyTable.CONTACT_NAME))
                            , authorized);
            privacyModelList.add(statusPrivacyModel);
        }
        cursor.close();
        return privacyModelList;
    }

    public List<String> getAllAuthorizedContacts() {
        database = this.getReadableDatabase();
        List<String> phone_numbers = new ArrayList<>();
        Cursor cursor = database.query(PrivacyTable.TABLE_NAME
                , new String[]{PrivacyTable.PHONE_NUMBER}
                , PrivacyTable.AUTHORIZED + " = ?"
                , new String[]{"1"}
                , null
                , null
                , null);
        while (cursor.moveToNext()) {
            phone_numbers.add(cursor.getString(cursor.getColumnIndex(PrivacyTable.PHONE_NUMBER)));
        }
        cursor.close();
        return phone_numbers;
    }
}
