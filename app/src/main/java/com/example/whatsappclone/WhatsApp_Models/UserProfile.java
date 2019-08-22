package com.example.whatsappclone.WhatsApp_Models;

public class UserProfile {
    private String onLineStatus;
    private String phoneNumber;
    private String uid;
    private ProfileImage profileImage;
    private Status status;

    public UserProfile() {
        //for fireBase firesStore
    }

    public UserProfile(String onLineStatus, String phoneNumber, String uid, ProfileImage profileImage, Status status) {
        this.onLineStatus = onLineStatus;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
        this.profileImage = profileImage;
        this.status = status;
    }

    public String getOnLineStatus() {
        return onLineStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUid() {
        return uid;
    }

    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public Status getStatus() {
        return status;
    }
}
