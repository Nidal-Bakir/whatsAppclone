package com.example.whatsappclone.WhatsAppFireStore;

import android.content.Context;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;

public class GetMessages  {
    private Context context;
    private DataBase dataBase;

    public GetMessages(Context context) {
        this.context = context;
         dataBase = new DataBase(context);
    }
    public void getMessagesFromFireStore(String phone_number){

    }
}
