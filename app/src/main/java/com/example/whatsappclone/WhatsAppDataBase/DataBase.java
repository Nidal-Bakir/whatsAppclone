package com.example.whatsappclone.WhatsAppDataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.example.whatsappclone.WhatsApp_Models.Profile_Status_img;
import com.example.whatsappclone.WhatsApp_Models.Status;

import java.util.ArrayList;
import java.util.List;

public class DataBase extends SQLiteOpenHelper {
    private static final String TAG = "DataBase";
    private static final int EMPTYCURSOR = 0;
    private static final String DB_NAME = "whatsApp_DB";
    private SQLiteDatabase database;

    /* Inner class that defines the Profiles_Status_img table contents  */
    private class Profiles_Status implements BaseColumns {
        private static final String TABLE_NAME = "Profiles_Status";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String IMAGE_PATH = "imagePath";
        private static final String IMAGE_URL = "imageUrl";
        private static final String STATUS_PATH = "statusPath";
        private static final String STATUS_URL = "statusUrl";

    }

    private static final String SQL_CREATE_PROFILES_STATUS_IMG =
            "CREATE TABLE " + Profiles_Status.TABLE_NAME
                    + " ("
                    + Profiles_Status.UID + " TEXT,"
                    + Profiles_Status.PHONE_NUMBER + " TEXT ,"
                    + Profiles_Status.IMAGE_PATH + " TEXT,"
                    + Profiles_Status.IMAGE_URL + " TEXT ,"
                    + Profiles_Status.STATUS_PATH + " TEXT,"
                    + Profiles_Status.STATUS_URL + " TEXT "
                    + ")";

    /* Inner class that defines the Contacts table contents  */
    private class Contacts implements BaseColumns {
        private static final String TABLE_NAME = "contacts";
        private static final String UID = "uid";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String CONTACT_NAME = "contact_name";
    }

    //this class for handel the contact result
    public static class Contact {
        private String UID;
        private String phone_number;
        private String contact_name;

        public Contact(String UID, String phone_number, String contact_name) {
            this.UID = UID;
            this.phone_number = phone_number;
            this.contact_name = contact_name;
        }

        public String getUID() {
            return UID;
        }

        public void setUID(String UID) {
            this.UID = UID;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public String getContact_name() {
            return contact_name;
        }
    }

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + Contacts.TABLE_NAME
                    + " ("
                    + Contacts.UID + " TEXT,"
                    + Contacts.PHONE_NUMBER + " TEXT,"
                    + Contacts.CONTACT_NAME + " TEXT "
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


    public DataBase(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILES_STATUS_IMG);
        db.execSQL(SQL_CREATE_CONTACTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TOdo:get the UIDs of tables to delete them
//        db.execSQL("DELETE ");
//        onCreate(db);
    }

    //this function will get the image for profile and status
    public Profile_Status_img getUserProfileAndStatus(String UID) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(Profiles_Status.TABLE_NAME
                , null
                , Profiles_Status.UID + " = ? "
                , new String[]{UID}
                , null
                , null
                , null);
        if (cursor.getCount() == EMPTYCURSOR)
            return null;
        cursor.moveToFirst();  //move to the element
        String imagePath = cursor.getString(cursor.getColumnIndex(Profiles_Status.IMAGE_PATH));
        String imageUrl = cursor.getString(cursor.getColumnIndex(Profiles_Status.IMAGE_URL));
        String statusPath = cursor.getString(cursor.getColumnIndex(Profiles_Status.STATUS_PATH));
        String statusUrl = cursor.getString(cursor.getColumnIndex(Profiles_Status.STATUS_URL));
        return new Profile_Status_img(
                new ProfileImage(imagePath, imageUrl)
                , new Status(statusPath, statusUrl));
    }

    public void insetUserProfileAndStatus(String UID, String phoneNumber, Profile_Status_img profile_status_img) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Profiles_Status.UID, UID);
        contentValues.put(Profiles_Status.PHONE_NUMBER, phoneNumber);
        //handel if the profile don't have profile image
        if (profile_status_img.getStatus() != null) {
            contentValues.put(Profiles_Status.STATUS_PATH, profile_status_img.getStatusPath());
            contentValues.put(Profiles_Status.STATUS_URL, profile_status_img.getStatusUrl());
        }
        //handel if the profile don't status
        if (profile_status_img.getProfileImage() != null) {
            contentValues.put(Profiles_Status.IMAGE_PATH, profile_status_img.getProfilePath());
            contentValues.put(Profiles_Status.IMAGE_URL, profile_status_img.getProfileUrl());
        }
        database.insert(Profiles_Status.TABLE_NAME, null, contentValues);


    }

    public void upDateProfileImage(String UID, ProfileImage profileImage) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Profiles_Status.IMAGE_PATH, profileImage.getImagePath());
        contentValues.put(Profiles_Status.IMAGE_URL, profileImage.getImageUrl());

        database.update(
                Profiles_Status.TABLE_NAME
                , contentValues
                , Profiles_Status.UID + " = ?"
                , new String[]{UID});
    }

    public void upDateSatusImage(String UID, Status status) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Profiles_Status.STATUS_PATH, status.getStatusPath());
        contentValues.put(Profiles_Status.STATUS_URL, status.getStatusUrl());
        database.update(
                Profiles_Status.TABLE_NAME
                , contentValues
                , Profiles_Status.UID + "= ?"
                , new String[]{UID});


    }

    public Contact getContact(String UID, String phone_number) {
        database = this.getReadableDatabase();
        Cursor cursor;
        //so i can search using UID OR phone number
        if (UID != null)
            cursor = database.query(
                    Contacts.TABLE_NAME
                    , null
                    , Contacts.UID + " = ?"
                    , new String[]{UID}
                    , null, null, null);
        else cursor = database.query(
                Contacts.TABLE_NAME
                , null
                , Contacts.PHONE_NUMBER + " = ?"
                , new String[]{phone_number}
                , null, null, null);
        if (cursor.getCount() != EMPTYCURSOR) {
            cursor.moveToFirst();
            return new Contact(
                    cursor.getString(cursor.getColumnIndex(Contacts.UID))
                    , cursor.getString(cursor.getColumnIndex(Contacts.PHONE_NUMBER))
                    , cursor.getString(cursor.getColumnIndex(Contacts.CONTACT_NAME))
            );
        }
        return null;
    }

    public List<Contact_Profile> getAllContact() {
        database = this.getReadableDatabase();
        List<Contact_Profile> contact_profiles = new ArrayList<>();
        Cursor cursor = database.query(Contacts.TABLE_NAME
                , null, null, null, null, null, Contacts.CONTACT_NAME);
        //loop throw all contact and get them
        while (cursor.moveToNext()) {
            Cursor ProfileCursor = database.query(Profiles_Status.TABLE_NAME
                    , new String[]{Profiles_Status.IMAGE_PATH, Profiles_Status.IMAGE_URL}
                    , Profiles_Status.UID + " = ?"
                    , new String[]{cursor.getString(cursor.getColumnIndex(Contacts.UID))}
                    , null
                    , null
                    , null);
            ProfileCursor.moveToFirst();
            //store the values
            ProfileImage profileImage = new ProfileImage(ProfileCursor.getString(ProfileCursor.getColumnIndex(Profiles_Status.IMAGE_PATH))
                    , ProfileCursor.getString(ProfileCursor.getColumnIndex(Profiles_Status.IMAGE_URL)));
            Contact contact = new Contact(cursor.getString(cursor.getColumnIndex(Contacts.UID))
                    , cursor.getString(cursor.getColumnIndex(Contacts.PHONE_NUMBER))
                    , cursor.getString(cursor.getColumnIndex(Contacts.CONTACT_NAME)));
            //add to list
            contact_profiles.add(new Contact_Profile(profileImage, contact));

        }
        return contact_profiles;
    }

    public boolean isContactTableEmpty() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(Contacts.TABLE_NAME
                , null, null, null, null, null, null);
        return cursor.getCount() == EMPTYCURSOR;
    }

    public void addContact(Contact contact) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contacts.UID, contact.UID);
        contentValues.put(Contacts.PHONE_NUMBER, contact.phone_number);
        contentValues.put(Contacts.CONTACT_NAME, contact.contact_name);
        database.insert(
                Contacts.TABLE_NAME
                , null
                , contentValues);

    }

    public void updateContact(String contactName, String UID, String phone_number) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contacts.CONTACT_NAME, contactName);
        //so i can update using UID OR phone number
        if (UID != null)
            database.update(
                    Contacts.TABLE_NAME
                    , contentValues
                    , Contacts.UID + " = ?"
                    , new String[]{UID});
        else database.update(
                Contacts.TABLE_NAME
                , contentValues
                , Contacts.PHONE_NUMBER + " = ?"
                , new String[]{phone_number});
    }


}
