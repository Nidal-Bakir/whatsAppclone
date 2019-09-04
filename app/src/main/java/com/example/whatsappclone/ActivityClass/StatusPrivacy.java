package com.example.whatsappclone.ActivityClass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.whatsappclone.Adapters.PrivacyAdapter;
import com.example.whatsappclone.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StatusPrivacy extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton floatingBt;
    private TextView count;
    private RecyclerView privacyRecyclerView;
    private PrivacyAdapter privacyAdapter;
    private static int countSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_privacy);
        toolbar = findViewById(R.id.statusPrivacyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_);
        setTitle(null);
        // contacts count
        count = findViewById(R.id.status_count_number);
        floatingBt = findViewById(R.id.savePrivacyFloatingBt);
        // save the selected contacts
        floatingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDO:save the change ;
            }
        });
        privacyRecyclerView = findViewById(R.id.statusPrivacyRecyclerView);
        privacyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //privacyAdapter=new PrivacyAdapter(this,)

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.status_privacy_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectAll:
                break;
            case R.id.search_status_privacy:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
