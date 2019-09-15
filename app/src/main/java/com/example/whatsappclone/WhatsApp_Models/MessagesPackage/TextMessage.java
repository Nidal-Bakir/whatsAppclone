package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public class TextMessage extends Message {
    private String textMessage;

    public TextMessage(int id, String phoneNumber, String messageUid, int messageState, long date, String textMessage) {
        super(id, phoneNumber, messageUid, messageState, date);
        this.textMessage = textMessage;
    }

    public String getTextMessage() {
        return textMessage;
    }
}
