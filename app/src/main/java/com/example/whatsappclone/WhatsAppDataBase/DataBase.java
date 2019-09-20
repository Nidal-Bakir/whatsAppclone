package com.example.whatsappclone.WhatsAppDataBase;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.whatsappclone.AssistanceClass.InternetCheck;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.GenralContact;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.StatusPrivacyModel;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;
import com.example.whatsappclone.WhatsApp_Models.WorkEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBase extends SQLiteOpenHelper {
    private static final String TAG = "DataBase";

    public enum MessageState {READ, DELIVERED, WAIT_NETWORK, NUN}

    public static final int MUTE = 1;
    public static final int NOT_MUTE = 0;
    private static final int MESSAGE_DELETED = -1;
    public static final int WAIT_NETWORK = 0;
    public static final int ON_SERVER = 1;
    public static final int DELIVERED = 2;
    public static final int READ = 3;
    private static final int EMPTYCURSOR = 0;
    private static final String DB_NAME = "whatsApp_DB";
    private static final long MILLI_SECOND_DAY = 86400000;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private CollectionReference statusCollectionRef =
            firestore.collection("profile").document(UserSettings.PHONENUMBER).collection("status");
    private SQLiteDatabase database;
    private ChatTableListener chatTableListener;

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

    //status privacy table
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

    // Status visit table
    private class StatusVisit implements BaseColumns {
        private static final String TABLE_NAME = "status_visit";
        private static final String ID = "id";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String TIME = "time";
    }

    private static final String SQL_CREATE_STATUS_VISIT_TABLE =
            "CREATE TABLE " + StatusVisit.TABLE_NAME
                    + " ("
                    + StatusVisit.ID + " INTEGER PRIMARY KEY,"
                    + StatusVisit.PHONE_NUMBER + " TEXT,"
                    + StatusVisit.TIME + " TEXT "
                    + ")";

    // the general table for any chat table
    private class ChatTable implements BaseColumns {
        private static final String ID = "_id";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String MESSAGE_UID = "message_uid";
        private static final String MESSAGE = "message";
        private static final String VOICE_URL = "voice_url";
        private static final String VOICE_PATH = "voice_path";
        private static final String IMAGE_URL = "image_url";
        private static final String IMAGE_PATH = "image_path";
        private static final String VIDEO_URL = "video_url";
        private static final String VIDEO_PATH = "video_path";
        private static final String FILE_URL = "file_url";
        private static final String FILE_PATH = "file_path";
        private static final String MESSAGE_STATE = "message_state";
        private static final String DATE = "date";

    }

    // hold all messages marked as WAIT_NETWORK
    private class MessagesHolderTable implements BaseColumns {
        private static final String TABLE_NAME = "messages_holder";
        private static final String ID = "_id";
        private static final String MESSAGE_UID = "message_uid";
        private static final String PHONE_NUMBER = "phone_number"; //table name ***
    }

    private static final String SQL_CREATE_MESSAGE_HOLDER_TABLE =
            "CREATE TABLE " + MessagesHolderTable.TABLE_NAME
                    + " ("
                    + MessagesHolderTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + MessagesHolderTable.MESSAGE_UID + " TEXT,"
                    + MessagesHolderTable.PHONE_NUMBER + " TEXT "//table name ***
                    + ")";

    private class ConversationTable implements BaseColumns {
        private static final String TABLE_NAME = "conversations";
        private static final String ID = "_id";
        private static final String PHONE_NUMBER = "phone_number";
        private static final String MUTE = "mute"; // if (1) mute notifications
        private static final String MESSAGES_COUNT = "messages_count";
        private static final String DATE = "date";
    }

    private static final String SQL_CREATE_CONVERSATION_TABLE =
            "CREATE TABLE " + ConversationTable.TABLE_NAME
                    + " ("
                    + ConversationTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ConversationTable.PHONE_NUMBER + " TEXT,"
                    + ConversationTable.MUTE + " INTEGER, " // if (1) mute notifications
                    + ConversationTable.MESSAGES_COUNT + " INTEGER, "
                    + ConversationTable.DATE + " INTEGER "
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
        db.execSQL(SQL_CREATE_STATUS_VISIT_TABLE);
        db.execSQL(SQL_CREATE_MESSAGE_HOLDER_TABLE);
        db.execSQL(SQL_CREATE_CONVERSATION_TABLE);
        //add user info to database the def info
        setDefaultUserInfo(new Contact(UserSettings.UID, UserSettings.PHONENUMBER, "Me", "online"), db);
        ProfileImage profileImage = new ProfileImage(null, null);
        setDefaultUserInfo(UserSettings.UID, UserSettings.PHONENUMBER, profileImage, db);
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
    private void setDefaultUserInfo(String UID, String phoneNumber, ProfileImage profileImage, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        //for profile table
        contentValues.put(ProfilesTable.UID, UID);
        contentValues.put(ProfilesTable.PHONE_NUMBER, phoneNumber);
        contentValues.put(ProfilesTable.IMAGE_PATH, profileImage.getImagePath());
        contentValues.put(ProfilesTable.IMAGE_URL, profileImage.getImageUrl());
        db.insert(ProfilesTable.TABLE_NAME, null, contentValues);
        // for status image
        contentValues = new ContentValues();
        contentValues.put(StatusTable.UID, UID);
        contentValues.put(StatusTable.PHONE_NUMBER, phoneNumber);
        db.insert(StatusTable.TABLE_NAME, null, contentValues);

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
    public ProfileImage getUserProfile(String UID, String phone_number) {
        database = this.getReadableDatabase();
        Cursor cursor;
        if (UID != null)
            cursor = database.query(ProfilesTable.TABLE_NAME
                    , null
                    , ProfilesTable.UID + " = ? "
                    , new String[]{UID}
                    , null
                    , null
                    , null);
        else
            cursor = database.query(ProfilesTable.TABLE_NAME
                    , null
                    , ProfilesTable.PHONE_NUMBER + " = ? "
                    , new String[]{phone_number}
                    , null
                    , null
                    , null);
        if (cursor.getCount() == EMPTYCURSOR) {
            cursor.close();
            return new ProfileImage(null, null);
        }
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
                , StatusTable.DATE + " DESC");
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
                    statusCollectionRef.document(phone_number).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                        }
                    });
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

    public void insertUserProfile(String UID, String phoneNumber, ProfileImage profileImage) {
        database = this.getWritableDatabase();
        ContentValues profileContentValues = new ContentValues();
        //for profile table
        profileContentValues.put(ProfilesTable.UID, UID);
        profileContentValues.put(ProfilesTable.PHONE_NUMBER, phoneNumber);

        //handel if the profile don't have profile image
        if (profileImage != null) {
            profileContentValues.put(ProfilesTable.IMAGE_PATH, profileImage.getImagePath());
            profileContentValues.put(ProfilesTable.IMAGE_URL, profileImage.getImageUrl());
        }
        database.insert(ProfilesTable.TABLE_NAME, null, profileContentValues);
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
        database = this.getReadableDatabase();
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

    public void addStatusImage(String UID, String phone_number) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatusTable.UID, UID);
        contentValues.put(StatusTable.PHONE_NUMBER, phone_number);
        database.insert(StatusTable.TABLE_NAME, null, contentValues);
    }


    public Contact getContact(String UID, String phone_number) {
        database = this.getReadableDatabase();
        Cursor cursor;
        //so i can search using MESSAGE_UID OR phone number
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
        //so i can update using MESSAGE_UID OR phone number
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
        //so i can search using MESSAGE_UID OR phone number
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
        //so i can update using MESSAGE_UID OR phone number
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
        //so i can update using MESSAGE_UID OR phone number
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

    public List<String> getAllAuthorizedContacts(boolean AuthorizedOnly) {
        database = this.getReadableDatabase();
        Cursor cursor;
        List<String> phone_numbers = new ArrayList<>();
        if (AuthorizedOnly) //will return only the Authorized users
            cursor = database.query(PrivacyTable.TABLE_NAME
                    , new String[]{PrivacyTable.PHONE_NUMBER}
                    , PrivacyTable.AUTHORIZED + " = ?"
                    , new String[]{"1"}
                    , null
                    , null
                    , null);
        else cursor = database.query(PrivacyTable.TABLE_NAME
                , new String[]{PrivacyTable.PHONE_NUMBER}
                , null
                , null
                , null
                , null
                , null);
        while (cursor.moveToNext()) {
            phone_numbers.add(cursor.getString(cursor.getColumnIndex(PrivacyTable.PHONE_NUMBER)));
        }
        cursor.close();
        return phone_numbers;
    }

    public void addVisit(VisitStatus visitStatus) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(StatusVisit.PHONE_NUMBER, visitStatus.getPhone_number());
        contentValues.put(StatusVisit.TIME, visitStatus.getTime());
        Cursor cursor =
                database.query(StatusVisit.TABLE_NAME
                        , null
                        , StatusVisit.PHONE_NUMBER + " =?"
                        , new String[]{visitStatus.getPhone_number()}
                        , null
                        , null
                        , null);
        if (cursor.getCount() == EMPTYCURSOR)
            database.insert(StatusVisit.TABLE_NAME
                    , null
                    , contentValues);
        cursor.close();

    }

    public List<VisitStatus> getAllVisits() {
        database = this.getReadableDatabase();
        VisitStatus visitStatus;
        List<VisitStatus> statusVisitList = new ArrayList<>();
        Cursor cursor =
                database.query(StatusVisit.TABLE_NAME
                        , new String[]{StatusVisit.PHONE_NUMBER, StatusVisit.TIME}
                        , null
                        , null
                        , null
                        , null
                        , StatusVisit.ID + " DESC");
        while (cursor.moveToNext()) {
            visitStatus = new VisitStatus(cursor.getString(cursor.getColumnIndex(StatusVisit.TIME)));
            visitStatus.setPhone_number(cursor.getString(cursor.getColumnIndex(StatusVisit.PHONE_NUMBER)));
            statusVisitList.add(visitStatus);
        }
        cursor.close();
        return statusVisitList;
    }

    public void deleteAllVisits() {
        database = this.getWritableDatabase();
        database.delete(StatusVisit.TABLE_NAME, null, null);
    }


    public void addMessageInChatTable(String userPhoneNumber, MessageModel messageModel) {
        // userPhoneNumber it is the same table name
        database = this.getReadableDatabase();
        database.execSQL("CREATE TABLE IF NOT EXISTS "
                + userPhoneNumber + " ( "
                + ChatTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ChatTable.PHONE_NUMBER + " TEXT,"
                + ChatTable.MESSAGE_UID + " TEXT, "
                + ChatTable.MESSAGE + " TEXT,"
                + ChatTable.VOICE_URL + " TEXT,"
                + ChatTable.VOICE_PATH + " TEXT,"
                + ChatTable.IMAGE_URL + " TEXT,"
                + ChatTable.IMAGE_PATH + " TEXT,"
                + ChatTable.VIDEO_URL + " TEXT,"
                + ChatTable.VIDEO_PATH + " TEXT,"
                + ChatTable.FILE_URL + " TEXT,"
                + ChatTable.FILE_PATH + " TEXT,"
                + ChatTable.MESSAGE_STATE + " INTEGER,"
                + ChatTable.DATE + " INTEGER"
                + " )"
        );
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatTable.PHONE_NUMBER, messageModel.getPhoneNumber());
        contentValues.put(ChatTable.MESSAGE_UID, messageModel.getMessageUid());
        contentValues.put(ChatTable.MESSAGE, messageModel.getTextMessage());
        contentValues.put(ChatTable.IMAGE_URL, messageModel.getImageUrl());
        contentValues.put(ChatTable.VOICE_URL, messageModel.getVoiceUrl());
        contentValues.put(ChatTable.VIDEO_URL, messageModel.getVideoUrl());
        contentValues.put(ChatTable.FILE_URL, messageModel.getFileUrl());
        if (messageModel.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
            if (InternetCheck.isOnline()) {
                if (!messageModel.getTextMessage().equals(null))
                    contentValues.put(ChatTable.MESSAGE_STATE, ON_SERVER);
                else contentValues.put(ChatTable.MESSAGE_STATE, WAIT_NETWORK);
            } else {
                contentValues.put(ChatTable.MESSAGE_STATE, WAIT_NETWORK);
                addMessageToMessageHolder(messageModel.getMessageUid(), userPhoneNumber);
            }
        } else {
            /*his message , i will use this value when i open chat activity and make every DELIVERED
             * message for his message read and notify the other user that i read his message
             */
            contentValues.put(ChatTable.MESSAGE_STATE, DELIVERED);
        }
        contentValues.put(ChatTable.DATE, Long.parseLong(messageModel.getDate()));
        database.insert(userPhoneNumber, null, contentValues);
        // send message that you are delivered the message
        WorkEvent workEvent;
        if (messageModel.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
            messageModel.setMessageState(ON_SERVER);
            // set the event to new message
            workEvent = new WorkEvent(UserSettings.PHONENUMBER, MessageState.NUN, true, false, messageModel);
            /*
             * add the message to user collection and other user collection
             * Because each user has their own copy of conversations
             * */
            firestore.collection("messages").document(UserSettings.PHONENUMBER)
                    .collection(userPhoneNumber).document(messageModel.getMessageUid()).set(messageModel);
            firestore.collection("messages").document(userPhoneNumber)
                    .collection(UserSettings.PHONENUMBER).document(messageModel.getMessageUid()).set(messageModel);
        } else {
            //tell the other user that you are received his message
            firestore.collection("messages").document(userPhoneNumber)
                    .collection(UserSettings.PHONENUMBER).document(messageModel.getMessageUid()).update("messageState", DELIVERED);
            workEvent = new WorkEvent(UserSettings.PHONENUMBER, MessageState.DELIVERED, false, false, null);
        }
        firestore.collection("profile")
                .document(messageModel.getPhoneNumber()).collection("event").add(workEvent);
        // add or update conversation
         Conversation conversation = addConversation(new Conversation(userPhoneNumber, NOT_MUTE, 0, 0), messageModel.getPhoneNumber());
        // update chat recycler view items
         chatTableListener.onAddNewMessage(workEvent.getMessageModel(), conversation);
    }

    public void chatTableListener(ChatTableListener chatTableListener) {
        this.chatTableListener = chatTableListener;
    }

    public interface ChatTableListener {
        void onAddNewMessage(MessageModel messageModel, Conversation conversation);

        void onDeleteOrChangeMessageState();
    }

    public Bundle getLastMessage(String phoneNumber) {
        Bundle bundle = new Bundle();
        database = this.getReadableDatabase();
        // phoneNumber <=> tableName
        Cursor cursor = database.
                rawQuery("SELECT * from " + phoneNumber + " ORDER BY " + ChatTable.DATE + " DESC LIMIT 1 "
                        , null);
        if (cursor.getCount() == EMPTYCURSOR) {
            cursor.close();
            return null;
        } else {
            cursor.moveToFirst();
            String messageOwner = cursor.getString(cursor.getColumnIndex(ChatTable.PHONE_NUMBER));
            bundle.putBoolean("isMyMessage", messageOwner.equals(UserSettings.PHONENUMBER));
            String message = cursor.getString(cursor.getColumnIndex(ChatTable.MESSAGE));
            int messageState = cursor.getInt(cursor.getColumnIndex(ChatTable.MESSAGE_STATE));

            String voiceUrl = cursor.getString(cursor.getColumnIndex(ChatTable.VOICE_URL));
            String voicePath = cursor.getString(cursor.getColumnIndex(ChatTable.VOICE_PATH));

            String imageUrl = cursor.getString(cursor.getColumnIndex(ChatTable.IMAGE_URL));
            String imagePath = cursor.getString(cursor.getColumnIndex(ChatTable.IMAGE_PATH));

            String videoUrl = cursor.getString(cursor.getColumnIndex(ChatTable.VIDEO_URL));
            String videoPath = cursor.getString(cursor.getColumnIndex(ChatTable.VIDEO_PATH));

            String fileUrl = cursor.getString(cursor.getColumnIndex(ChatTable.FILE_URL));
            String filePath = cursor.getString(cursor.getColumnIndex(ChatTable.FILE_PATH));

            if (!message.equals("")) {
                bundle.putString("message", message);
            } else if (!imagePath.equals("") || !imageUrl.equals("")) {
                bundle.putString("message", "Image");
            } else if (!voicePath.equals("") || !voiceUrl.equals("")) {
                bundle.putString("message", "Voice message");
            } else if (!videoPath.equals("") || !videoUrl.equals("")) {
                bundle.putString("message", "Video");
            } else if (!filePath.equals("") || !fileUrl.equals("")) {
                bundle.putString("message", "File");
            }

            bundle.putInt("messageState", messageState);
            return bundle;
        }

    }

    public void updateMessageState(String phoneNumber, MessageState messageState) {
        database = this.getReadableDatabase();
        //if the last message is the other user message then return
        Cursor cursor = database.
                rawQuery("SELECT * from " + phoneNumber + " ORDER BY " + ChatTable.DATE + " DESC LIMIT 1 "
                        , null);
        cursor.moveToFirst();
        if (cursor.getString(cursor.getColumnIndex(ChatTable.PHONE_NUMBER)).equals(phoneNumber)) {
            cursor.close();
            return;
        } else {
            if (messageState.equals(MessageState.DELIVERED)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ChatTable.MESSAGE_STATE, DELIVERED);
                database.update(phoneNumber
                        , contentValues
                        , ChatTable.PHONE_NUMBER + " =? " + " AND " + ChatTable.MESSAGE_STATE + " =? "
                        , new String[]{UserSettings.PHONENUMBER, String.valueOf(ON_SERVER)});
            } else if (messageState.equals(MessageState.READ)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(ChatTable.MESSAGE_STATE, READ);
                database.update(phoneNumber
                        , contentValues
                        , ChatTable.PHONE_NUMBER + " =? " + " AND " + ChatTable.MESSAGE_STATE + " =? "
                        , new String[]{UserSettings.PHONENUMBER, String.valueOf(DELIVERED)});
            }
            chatTableListener.onDeleteOrChangeMessageState();

        }
        cursor.close();
    }

    public void deleteMessage(String userPhoneNumber, MessageModel messageModel) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatTable.MESSAGE, "This message was deleted.");
        contentValues.put(ChatTable.MESSAGE_STATE, MESSAGE_DELETED);
        database.update(userPhoneNumber
                , contentValues
                , ChatTable.MESSAGE_UID + " =? "
                , new String[]{messageModel.getMessageUid()});
        // if the message is my message i need to delete
        if (messageModel.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
            Map<String, Object> map = new HashMap<>();
            map.put("messageState", MESSAGE_DELETED);
            map.put("textMessage", "This message was deleted.");
            firestore.collection("messages").document(UserSettings.PHONENUMBER)
                    .collection(userPhoneNumber).document(messageModel.getMessageUid()).update(map);
            firestore.collection("messages").document(userPhoneNumber)
                    .collection(UserSettings.PHONENUMBER).document(messageModel.getMessageUid()).update(map);
            WorkEvent workEvent = new WorkEvent(UserSettings.PHONENUMBER, MessageState.NUN, false, true, messageModel);
            firestore.collection("profile")
                    .document(userPhoneNumber).collection("event").add(workEvent);

        }
        // update chat recycler view items using diffUtil
        chatTableListener.onDeleteOrChangeMessageState();
    }

    private void addMessageToMessageHolder(String messageUid, String tableName) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessagesHolderTable.MESSAGE_UID, messageUid);
        contentValues.put(MessagesHolderTable.PHONE_NUMBER, tableName);
        database.insert(MessagesHolderTable.TABLE_NAME, null, contentValues);
    }

    public void updateAllHoledMessages() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(
                MessagesHolderTable.TABLE_NAME
                , null
                , null
                , null
                , null
                , null
                , null);
        if (cursor.getCount() == EMPTYCURSOR) {
            cursor.close();
        } else {
            database = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            while (cursor.moveToNext()) {
                String messageUdi = cursor.getString(cursor.getColumnIndex(MessagesHolderTable.MESSAGE_UID));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(MessagesHolderTable.PHONE_NUMBER));
                contentValues.put(ChatTable.MESSAGE_STATE, ON_SERVER);
                database.update(phoneNumber, contentValues
                        , ChatTable.MESSAGE_UID + " =?" + " AND " + ChatTable.MESSAGE_STATE + " =?"
                        , new String[]{messageUdi, String.valueOf(WAIT_NETWORK)});
                updateMessageState(phoneNumber, MessageState.WAIT_NETWORK);
            }
            cursor.close();

            // we do not need this messages anymore
            database.delete(MessagesHolderTable.TABLE_NAME, null, null);

        }

    }

    // Conversation class for hold
    public static class Conversation {
        private String phoenNamber;
        private int mute;
        private int messageCount;
        private long date;

        public Conversation(String phoneNumber, int mute, int messageCount, long date) {
            this.phoenNamber = phoneNumber;
            this.mute = mute;
            this.messageCount = messageCount;
            this.date = date;
        }

        public String getPhoneNumber() {
            return phoenNamber;
        }

        public int getMute() {
            return mute;
        }

        public int getMessageCount() {
            return messageCount;
        }

        public long getDate() {
            return date;
        }
    }

    public Conversation addConversation(Conversation conversation, String messageOwner) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        Calendar calendar = Calendar.getInstance();
        Cursor cursor = database.query(
                ConversationTable.TABLE_NAME
                , null
                , ConversationTable.PHONE_NUMBER + " =?"
                , new String[]{conversation.phoenNamber}
                , null
                , null
                , null);
        database = this.getWritableDatabase();
        // add one to the message count if is exist
        if (cursor.getCount() != EMPTYCURSOR) {
            cursor.moveToFirst();

            if (!messageOwner.equals(UserSettings.PHONENUMBER)) {
                int messageCount = cursor.getInt(cursor.getColumnIndex(ConversationTable.MESSAGES_COUNT));
                ++messageCount;
                contentValues.put(ConversationTable.MESSAGES_COUNT, messageCount);
            } else {
                contentValues.put(ConversationTable.MESSAGES_COUNT, 0);
            }
            contentValues.put(ConversationTable.DATE, calendar.getTimeInMillis());
            database.update(
                    ConversationTable.TABLE_NAME
                    , contentValues
                    , ConversationTable.PHONE_NUMBER + " =?"
                    , new String[]{conversation.getPhoneNumber()});
        } else {
            //create row for this Conversation
            contentValues.put(ConversationTable.PHONE_NUMBER, conversation.getPhoneNumber());
            contentValues.put(ConversationTable.MUTE, NOT_MUTE);
            if (messageOwner.equals(UserSettings.PHONENUMBER))
                contentValues.put(ConversationTable.MESSAGES_COUNT, 0);
            else
                contentValues.put(ConversationTable.MESSAGES_COUNT, 1);
            contentValues.put(ConversationTable.DATE, calendar.getTimeInMillis());
            database.insert(
                    ConversationTable.TABLE_NAME
                    , null
                    , contentValues);

        }
        database = this.getReadableDatabase();
        cursor = database.
                rawQuery("SELECT * from " + ConversationTable.TABLE_NAME + " ORDER BY " + ConversationTable.DATE + " DESC LIMIT 1 "
                        , null);
        String phoneNumber = cursor.getString(cursor.getColumnIndex(ConversationTable.PHONE_NUMBER));
        int messageCount = cursor.getInt(cursor.getColumnIndex(ConversationTable.MESSAGES_COUNT));
        int mute = cursor.getInt(cursor.getColumnIndex(ConversationTable.MUTE));
        long date = cursor.getLong(cursor.getColumnIndex(ConversationTable.DATE));
        Conversation conversation1 = new Conversation(phoneNumber, mute, messageCount, date);
        cursor.close();
        return conversation1;

    }

    public void reSetMessageCount(String phoneNumber) {
        database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConversationTable.MESSAGES_COUNT, 0);
        database.update(
                ConversationTable.TABLE_NAME
                , contentValues
                , ConversationTable.PHONE_NUMBER + " =?"
                , new String[]{phoneNumber});
    }

    public void deleteConversation(Conversation conversation) {
        database = this.getWritableDatabase();
        database.delete(
                ConversationTable.TABLE_NAME
                , ConversationTable.PHONE_NUMBER + " =?"
                , new String[]{conversation.getPhoneNumber()});
    }

    public List<Conversation> getAllConversation() {
        database = this.getReadableDatabase();
        Conversation conversation;
        List<Conversation> conversationList = new ArrayList<>();
        Cursor cursor = database.query(
                ConversationTable.TABLE_NAME
                , null
                , null
                , null
                , null
                , null
                , ConversationTable.DATE + " DESC");
        if (cursor.getCount() == EMPTYCURSOR) {
            cursor.close();
            return conversationList;
        } else {
            while (cursor.moveToNext()) {
                conversation = new Conversation(cursor.getString(cursor.getColumnIndex(ConversationTable.PHONE_NUMBER))
                        , cursor.getInt(cursor.getColumnIndex(ConversationTable.MUTE))
                        , cursor.getInt(cursor.getColumnIndex(ConversationTable.MESSAGES_COUNT))
                        , cursor.getLong(cursor.getColumnIndex(ConversationTable.DATE)));
                conversationList.add(conversation);
            }
            cursor.close();
            return conversationList;
        }
    }

    public List<String> getAllMutedConversations() {
        database = this.getReadableDatabase();
        List<String> numberList = new ArrayList<>();
        Cursor cursor = database.query(
                ConversationTable.TABLE_NAME
                , new String[]{ConversationTable.PHONE_NUMBER}
                , ConversationTable.MUTE + " =?"
                , new String[]{String.valueOf(MUTE)}
                , null
                , null
                , null);
        while (cursor.moveToNext()) {
            numberList.add(cursor.getString(cursor.getColumnIndex(ConversationTable.PHONE_NUMBER)));
        }
        cursor.close();
        return numberList;
    }
}
