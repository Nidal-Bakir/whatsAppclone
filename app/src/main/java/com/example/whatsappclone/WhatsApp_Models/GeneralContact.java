package com.example.whatsappclone.WhatsApp_Models;

public class GeneralContact {
    private String UID;
    private String phone_number;
    private String contact_name;

    public GeneralContact(String UID, String phone_number, String contact_name) {
        this.UID = UID;
        this.phone_number = phone_number;
        this.contact_name = contact_name;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }
}
