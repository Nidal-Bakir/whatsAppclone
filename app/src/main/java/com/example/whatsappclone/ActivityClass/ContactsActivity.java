package com.example.whatsappclone.ActivityClass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.SyncContactsWithCloudDB;
import com.example.whatsappclone.WhatsApp_Models.ContactsAdapter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ContactsAdapter adapter;
    List<DataBase.Contact_Profile> contact_profiles;
    DataBase dataBase;
    SyncContactsWithCloudDB syncContactsWithCloudDB = null;
    private static final String TAG = "ContactsActivity";
    String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = findViewById(R.id.contactToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_);
        setTitle(null);
        final TextView contacts_count = findViewById(R.id.contacts_count);
        //get country code
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso();
        //you can not sync if there is no internet
        if (InternetCheck.isOnline()) {
            syncContactsWithCloudDB = new SyncContactsWithCloudDB(getApplicationContext(), countryCode);
            syncContactsWithCloudDB.execute(true);
            syncContactsWithCloudDB.setOnSyncFinish(new SyncContactsWithCloudDB.OnSyncFinish() {
                @Override
                public void onFinish(List<DataBase.Contact_Profile> contact_profiles) {
                    adapter.setContact_profiles(contact_profiles);
                    adapter.notifyDataSetChanged();
                    //set the number of contacts
                    contacts_count.setText(String.valueOf(contact_profiles.size()) + " contacts");
                }
            });

        }
        //connect to data base
        dataBase = new DataBase(this);
        contact_profiles = dataBase.getAllContact();
        recyclerView = findViewById(R.id.contact_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(this, contact_profiles);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onClick(DataBase.Contact contact) {
                finish();
                //TODO :: go to chat activity for start new conversation
                Toast.makeText(ContactsActivity.this
                        , contact.getUID()
                                + "::" + contact.getPhone_number()
                                + "::" + contact.getContact_name()
                        , Toast.LENGTH_LONG).show();
            }
        });
        //set the number of contacts
        contacts_count.setText(String.valueOf(contact_profiles.size()) + " contacts");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contats_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (syncContactsWithCloudDB != null)
            syncContactsWithCloudDB.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                if (syncContactsWithCloudDB != null) {
                    syncContactsWithCloudDB.cancel(true);
                    syncContactsWithCloudDB.execute(true);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
