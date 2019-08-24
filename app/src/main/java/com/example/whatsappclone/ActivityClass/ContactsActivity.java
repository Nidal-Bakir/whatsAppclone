package com.example.whatsappclone.ActivityClass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.ContactsAdapter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ContactsAdapter adapter;
    List<DataBase.Contact_Profile> contact_profiles;
    DataBase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = findViewById(R.id.contactToolbar);
        setSupportActionBar(toolbar);
        setTitle(null);
        dataBase = new DataBase(this);
        contact_profiles = dataBase.getAllContact();
        recyclerView = findViewById(R.id.contact_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contact_profiles, this);
        recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onClick(DataBase.Contact contact) {
                finish();
                Toast.makeText(ContactsActivity.this
                        , contact.getUID()
                                + "::" + contact.getPhone_number()
                                + "::" + contact.getContact_name()
                        , Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
