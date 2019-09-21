package com.example.whatsappclone.WhatsApp_Models;

public class StatusPrivacyModel extends GeneralContact {
    boolean authorized;
    public StatusPrivacyModel(String UID, String phone_number, String contact_name, boolean authorized) {
        super(UID, phone_number, contact_name);
        this.authorized=authorized;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }
}
