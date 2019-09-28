package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

import android.net.Uri;

import com.google.firebase.storage.UploadTask;

public class ImageMessage extends Message {
    private  String ImageUrl ;
    private Uri ImagePath;
    private UploadTask uploadTask;

    public UploadTask getUploadTask() {
        return uploadTask;
    }

    public void setUploadTask(UploadTask uploadTask) {
        this.uploadTask = uploadTask;
    }

    public ImageMessage(int id, String phoneNumber, String messageUid, int messageState, long date, String imageUrl, Uri imagePath) {
        super(id, phoneNumber, messageUid, messageState, date);
        ImageUrl = imageUrl;
        ImagePath = imagePath;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public Uri getImagePath() {
        return ImagePath;
    }
}
