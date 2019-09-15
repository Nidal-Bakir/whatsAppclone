package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public class VideoMessage extends Message {
    private String videoUrl;
    private String  videoPath;

    public VideoMessage(int id, String phoneNumber, String messageUid, int messageState, long date, String videoUrl, String videoPath) {
        super(id, phoneNumber, messageUid, messageState, date);
        this.videoUrl = videoUrl;
        this.videoPath = videoPath;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getVideoPath() {
        return videoPath;
    }
}
