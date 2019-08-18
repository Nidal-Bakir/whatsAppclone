package com.example.whatsappclone;

public class Status {
    private String statusPath;
    private String statusUrl;

    public Status() {
        //for fireStore
    }

    public Status(String statusPath, String statusUrl) {
        this.statusPath = statusPath;
        this.statusUrl = statusUrl;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public String getStatusUrl() {
        return statusUrl;
    }
}
