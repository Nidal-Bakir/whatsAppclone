package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public class FileMessage extends Message {
    private String fileUrl;
    private String  filePath;

    public FileMessage(int id, String phoneNumber, String messageUid, int messageState, long date, String fileUrl, String filePath) {
        super(id, phoneNumber, messageUid, messageState, date);
        this.fileUrl = fileUrl;
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }
}
