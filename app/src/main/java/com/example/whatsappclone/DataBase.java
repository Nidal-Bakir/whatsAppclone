package com.example.whatsappclone;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.Nullable;

public class DataBase extends SQLiteOpenHelper {
    private static final String TAG = "DataBase";

    private static final String DB_NAME = "whatsApp_DB";

    /* Inner class that defines the Profiles_Status_img table contents  */
    private class Profiles_Status implements BaseColumns {
        private static final String TABLE_NAME = "Profiles_Image";
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

    private static final String SQL_CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + Contacts.TABLE_NAME
                    + " ("
                    + Contacts.UID + " TEXT,"
                    + Contacts.PHONE_NUMBER + " TEXT,"
                    + Contacts.CONTACT_NAME + " TEXT "
                    + ")";


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

    //this will get the image for profile and status
    public Profile_Status_img getUserProfileAndStatus(String UID) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.query(Profiles_Status.TABLE_NAME
                , null
                , Profiles_Status.UID + " = ? "
                , new String[]{UID}
                , null
                , null
                , null);
        String imagePath = cursor.getString(cursor.getColumnIndex(Profiles_Status.IMAGE_PATH));
        Log.d(TAG, "getUserProfileAndStatus: " + imagePath);
        String imageUrl = cursor.getString(cursor.getColumnIndex(Profiles_Status.IMAGE_URL));
        String statusPath = cursor.getString(cursor.getColumnIndex(Profiles_Status.STATUS_PATH));
        String statusUrl = cursor.getString(cursor.getColumnIndex(Profiles_Status.STATUS_URL));
        return new Profile_Status_img(
                new ProfileImage(imagePath, imageUrl)
                , new Status(statusPath, statusUrl));
    }

    public void insetUserProfileAndStatus(String UID, String phoneNumber, Profile_Status_img profile_status_img) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Profiles_Status.UID, UID);
        contentValues.put(Profiles_Status.PHONE_NUMBER, phoneNumber);
        contentValues.put(Profiles_Status.IMAGE_PATH, profile_status_img.getProfilePath());
        contentValues.put(Profiles_Status.IMAGE_URL, profile_status_img.getProfileUrl());
        contentValues.put(Profiles_Status.STATUS_PATH, profile_status_img.getStatusPath());
        contentValues.put(Profiles_Status.STATUS_URL, profile_status_img.getStatusUrl());
        database.insert(Profiles_Status.TABLE_NAME, null, contentValues);


    }

    public void upDateProfileImage(String UID, String profilePath, String profileUrl) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Profiles_Status.IMAGE_PATH, profilePath);
        contentValues.put(Profiles_Status.IMAGE_URL, profileUrl);
        database.update(
                Profiles_Status.TABLE_NAME
                , contentValues
                , Profiles_Status.UID + " = ?"
                , new String[]{UID});
    }

}
