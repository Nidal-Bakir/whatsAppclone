package com.example.whatsappclone.WhatsApp_Models.MessagesPackage;

public class VoiceMessage extends Message {
    private String voiceUrl;
    private String voicePath;

    public VoiceMessage(int id, String phoneNumber, String messageUid, int messageState, long date) {
        super(id, phoneNumber, messageUid, messageState, date);
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public String getVoicePath() {
        return voicePath;
    }
}
