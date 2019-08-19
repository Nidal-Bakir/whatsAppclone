package com.example.whatsappclone;

import android.os.AsyncTask;

public class SyncContactsWithClouldDB extends AsyncTask<Boolean, Void, Void> {
    /**
     * this class is the responsible for download the profile images
     * and the Status image
     **/
    @Override
    protected void onPreExecute() {
        //read the contacts from phone
    }

    @Override
    protected Void doInBackground(Boolean... booleans) {
        if (booleans[0]) {
            //read all contacts in phone and sync them
            //and add the new contacts to data base if there is any **
            // and Check the compatibility between contacts names in Db and user contacts names
        } else {
            //if the Contacts_Table is Empty the contacts will sync
            // and Check the compatibility between contacts names in Db and user contacts names
        }
        return null;
    }

}
