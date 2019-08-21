package com.example.whatsappclone.WhatsApp_Models;


public class ProfileImage {
    private String imagePath;
    private String imageUrl;

    public ProfileImage() {
        //for fireStore
    }

    public ProfileImage(String imagePath, String imageUrl) {
        this.imagePath = imagePath;
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
