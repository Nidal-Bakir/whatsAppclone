package com.example.whatsappclone.WhatsApp_Models;

import com.google.firebase.firestore.Exclude;

public class VisitStatus {
    String time;
    String phone_number;

    public VisitStatus() {
        //for fireStore
    }

    public VisitStatus(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
    @Exclude
    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
