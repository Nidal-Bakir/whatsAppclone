package com.example.whatsappclone;

public class Profile_Status_img {
    private ProfileImage profileImage;
    private Status status;

    public Profile_Status_img(ProfileImage profileImage, Status status) {
        this.profileImage = profileImage;
        this.status = status;
    }

    public String getProfilePath() {
        return profileImage.getImagePath();
    }
    public String getProfileUrl() {
        return profileImage.getImageUrl();
    }
    public String getStatusPath() {
        return status.getStatusPath();
    }
    public String getStatusUrl() {
        return status.getStatusUrl();
    }

}