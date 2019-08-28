package com.example.whatsappclone.WhatsApp_Models;

public class Status {
    private String statusPath;
    private String statusUrl;
    private String date;

    public Status() {
        //for fireStore
    }

    public Status(String statusPath, String statusUrl,String date) {
        this.statusPath = statusPath;
        this.statusUrl = statusUrl;
        this.date=date;
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
}
