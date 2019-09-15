package com.example.whatsappclone.WhatsApp_Models;

public class WorkEvent {
    private String phoneNumber;
    private boolean readOrDelivered;
    private boolean deleteMessage;
    private boolean newMessage;
    private MessageModel messageModel;

    public WorkEvent() {
    }

    public WorkEvent(String phoneNumber, boolean readOrDelivered, boolean newMessage,boolean deleteMessage , MessageModel messageModel) {
        this.phoneNumber = phoneNumber;
        this.readOrDelivered = readOrDelivered;
        this.newMessage = newMessage;
        this.deleteMessage=deleteMessage;
        this.messageModel = messageModel;
    }

    public boolean isDeleteMessage() {
        return deleteMessage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isReadOrDelivered() {
        return readOrDelivered;
    }

    public boolean isNewMessage() {
        return newMessage;
    }

    public MessageModel getMessageModel() {
        return messageModel;
    }
}
