package com.example.whatsappclone;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

public class DataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "whatsApp_DB";

    /* Inner class that defines the table contents of Profiles_Image table */
    private static class Profiles_Image implements BaseColumns {
        private static final String TABEL_NAME = "Profiles_Image";
        public static final String UID = "uid";
        public static final String IMAGEPATH = "imagePath";
        public static final String IMAGEURL = "imageUrl";
    }

    private static final String SQL_CREATE_PROFILES_IMAGE =
            "CREATE TABLE " + Profiles_Image.TABEL_NAME + " (" +
                    Profiles_Image.UID + " TEXT," + Profiles_Image.IMAGEPATH + " TEXT,"
                    + Profiles_Image.IMAGEURL + " TEXT )";

    public DataBase(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
          db.execSQL(SQL_CREATE_PROFILES_IMAGE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TOdo:get the UIDs of tables to delete them
//        db.execSQL("DELETE ");
//        onCreate(db);
    }
}
