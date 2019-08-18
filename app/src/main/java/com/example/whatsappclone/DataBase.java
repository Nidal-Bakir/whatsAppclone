package com.example.whatsappclone;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

public class DataBase extends SQLiteOpenHelper {

    private static final String DB_NAME  = "whatsApp_DB";
    private static final String SQL_CREATE_MY_FROFILE_TABLE  = "CREATE TABLE ";

    /* Inner class that defines the table contents */
    public static class Profile implements BaseColumns {
        private static final String TABEL_NAME  = "my_profile";
        public static final String UID = "title";
        public static final String PHONE_NUMBER = "subtitle";

    }
    public DataBase(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      //  db.execSQL();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
