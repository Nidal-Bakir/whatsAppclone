package com.example.whatsappclone.ActivityClass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.AssistanceClass.InternetCheck;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.SyncContactsWithCloudDB;
import com.example.whatsappclone.Adapters.ContactsAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ContactsAdapter adapter;
    List<DataBase.Contact_Profile> contact_profiles;
    DataBase dataBase;
    ProgressBar progressBar;
    TextView contacts_count;
    SyncContactsWithCloudDB syncContactsWithCloudDB = null;
    private static final String TAG = "ContactsActivity";
    String countryCode;
    private static final int INVITE_FRIEND_CODE = 558;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == INVITE_FRIEND_CODE) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("smsto:"));
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address"  , cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                smsIntent.putExtra("sms_body"  , "Test ");
                startActivity(smsIntent);
            }
        }

    }

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
        contacts_count = findViewById(R.id.contacts_count);
        progressBar = findViewById(R.id.progressBar);
        /* change progress Bar color  */
        Drawable drawable = progressBar.getIndeterminateDrawable().mutate();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(drawable);
        //get country code
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso();
        //sync the contacts
        sync();
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
                Intent intent=new Intent(ContactsActivity.this,chatActivity.class);
                intent.putExtra("phone_number",contact.getPhone_number());
                intent.putExtra("uid",contact.getUID());
                intent.putExtra("contact_name",contact.getContact_name());
                startActivity(intent);

            }
        });
        //set the number of contacts
        contacts_count.setText(String.valueOf(contact_profiles.size()) + " contacts");

    }

    private void sync() {
        //you can not sync if there is no internet
        InternetCheck internetCheck=new InternetCheck(this);
        internetCheck.execute();
        internetCheck.onComplete(new InternetCheck.OnCheckComplete() {
            @Override
            public void onCheckComplete(boolean isOnline) {
                if (isOnline) {
                    syncContactsWithCloudDB = new SyncContactsWithCloudDB(getApplicationContext(), countryCode);
                    syncContactsWithCloudDB.execute(true);
                    syncContactsWithCloudDB.setOnSyncFinish(new SyncContactsWithCloudDB.OnSyncFinish() {
                        @Override
                        public void onFinish(List<DataBase.Contact_Profile> contact_profiles) {
                            adapter.setContact_profiles(contact_profiles);
                            adapter.notifyDataSetChanged();
                            //set the number of contacts
                            contacts_count.setText(String.valueOf(contact_profiles.size()) + " contacts");
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
            }
        });

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
                    sync();
                    progressBar.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.openPhoneContacts:
                startActivity(new Intent(Intent.ACTION_DEFAULT, ContactsContract.Contacts.CONTENT_URI));
                break;
            case R.id.invite_friend:

               Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent,INVITE_FRIEND_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
