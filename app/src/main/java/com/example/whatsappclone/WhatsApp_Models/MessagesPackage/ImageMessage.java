package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public class ImageMessage extends Message {
    private  String ImageUrl ;
    private String ImagePath;

    public ImageMessage(int id, String phoneNumber, String messageUid, int messageState, long date, String imageUrl, String imagePath) {
        super(id, phoneNumber, messageUid, messageState, date);
        ImageUrl = imageUrl;
        ImagePath = imagePath;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getImagePath() {
        return ImagePath;
    }
}
