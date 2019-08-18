package com.example.whatsappclone;


public class ProfileImage {
    private String imagepath;
    private String imageUrl;

    public ProfileImage() {
        //for fireStore
    }
    public ProfileImage(String imagepath, String imageUrl) {
        this.imagepath = imagepath;
        this.imageUrl = imageUrl;
    }

    public String getImagepath() {
        return imagepath;
    }

    public String getImageUrl() {
        return imageUrl;
    }


}
