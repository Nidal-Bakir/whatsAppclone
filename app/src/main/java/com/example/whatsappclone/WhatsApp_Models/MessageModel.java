package com.example.whatsappclone.WhatsApp_Models;

import android.net.Uri;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.storage.UploadTask;

public class MessageModel {
    private String phoneNumber;
    private String messageUid;
    private String textMessage;
    private String voiceUrl;
    private String imageUrl;
    private String videoUrl;
    private String fileUrl;
    private int messageState;
    private String date;
    private String voicePath;
    private Uri imagePath;
    private String videoPath;
    private String filePath;
    private UploadTask uploadTask;

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    @Exclude
    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    @Exclude
    public Uri getImagePath() {
        return imagePath;
    }

    public void setImagePath(Uri imagePath) {
        this.imagePath = imagePath;
    }

    @Exclude
    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Exclude
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public MessageModel() {
    }

    public MessageModel(String phoneNumber, String messageUid, String textMessage, String voiceUrl, String imageUrl, String videoUrl, String fileUrl, String date) {
        this.phoneNumber = phoneNumber;
        this.messageUid = messageUid;
        this.textMessage = textMessage;
        this.voiceUrl = voiceUrl;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.fileUrl = fileUrl;
        this.date = date;
    }

    public int getMessageState() {
        return messageState;
    }

    public void setMessageState(int messageState) {
        this.messageState = messageState;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getDate() {
        return date;
    }

    public void setMessageUid(String messageUid) {
        this.messageUid = messageUid;
    }

    @Exclude
    public UploadTask getUploadTask() {
        return uploadTask;
    }

    public void setUploadTask(UploadTask uploadTask) {
        this.uploadTask = uploadTask;
    }
}
