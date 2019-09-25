package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public abstract class Message {
    private int id;
    private String messageUid;
    private long date;
    private int messageState;
    private String phoneNumber;



    public Message(int id, String phoneNumber, String messageUid, int messageState, long date) {
        this.id = id;
        this.messageUid = messageUid;
        this.date = date;
        this.messageState = messageState;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public String getMessageUid() {
        return messageUid;
    }

    public long getDate() {
        return date;
    }

    public int getMessageState() {
        return messageState;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setMessageState(int messageState) {
        this.messageState = messageState;
    }
}
