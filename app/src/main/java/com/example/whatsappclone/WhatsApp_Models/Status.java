package com.example.whatsappclone.WhatsApp_Models;

import com.google.firebase.firestore.Exclude;

public class Status {
    private String statusPath;
    private String statusUrl;
    private String date;
    private String phone_number; // we will use the phone number to delete the status or add status
                                // and the most important thing for Identify the status
    public Status() {
        //for fireStore
    }

    public Status(String statusPath, String statusUrl, String date) {
        this.statusPath = statusPath;
        this.statusUrl = statusUrl;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public String getStatusUrl() {
        return statusUrl;
    }
    @Exclude
    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}
