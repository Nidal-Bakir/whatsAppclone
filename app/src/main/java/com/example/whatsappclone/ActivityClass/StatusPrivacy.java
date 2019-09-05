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
import android.widget.Toast;

import com.example.whatsappclone.Adapters.PrivacyAdapter;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.StatusPrivacyModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Iterator;
import java.util.List;

public class StatusPrivacy extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton floatingBt;
    private TextView countTextView;
    private RecyclerView privacyRecyclerView;
    private PrivacyAdapter privacyAdapter;
    private int countSelected = 0;
    private DataBase dataBase;
    private List<StatusPrivacyModel> privacyModelList;
    private final int AUTHORIZED = 1;
    private final int NOTAUTHORIZED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) throws IllegalStateException {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_privacy);
        toolbar = findViewById(R.id.statusPrivacyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_);
        setTitle(null);
        dataBase = new DataBase(this);
        // elected contacts count
        countTextView = findViewById(R.id.status_count_number);
        floatingBt = findViewById(R.id.savePrivacyFloatingBt);
        // save the selected contacts
        floatingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<StatusPrivacyModel> modelsDataSet = privacyAdapter.getDatasetFromAdapter();
                Iterator<StatusPrivacyModel> iterator = modelsDataSet.iterator();
                while (iterator.hasNext()) {
                    StatusPrivacyModel iteratormodel = iterator.next();
                    if (iteratormodel.isAuthorized()) {
                        dataBase.upDateAuthorizedValue(AUTHORIZED, iteratormodel.getUID(), null);
                    } else {
                        dataBase.upDateAuthorizedValue(NOTAUTHORIZED, iteratormodel.getUID(), null);
                    }
                }
                finish();
                Toast.makeText(StatusPrivacy.this, "Your privacy has been updated", Toast.LENGTH_SHORT).show();
            }
        });
        privacyRecyclerView = findViewById(R.id.statusPrivacyRecyclerView);
        privacyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        privacyModelList = dataBase.getAllContactsInStatusPrivacy();
        privacyAdapter = new PrivacyAdapter(this, privacyModelList);
        privacyRecyclerView.setAdapter(privacyAdapter);
        privacyAdapter.onItemClickListener(new PrivacyAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                boolean authorized = privacyAdapter.changSelection(position);
                if (authorized) {
                    countSelected++;
                } else {
                    countSelected--;
                }
                upDateCountTextView();
            }

            @Override
            public void onCheckChanged(int position, boolean isChecked) {
                privacyAdapter.changeAuthorizedInDataSet(position, isChecked);
                if (isChecked) {
                    countSelected++;
                } else {
                    countSelected--;
                }
                upDateCountTextView();
            }
        });
        setCountOfSelection(privacyModelList);


    }

    private void upDateCountTextView() {
        if (countSelected == 0)
            countTextView.setText("No one can see your status");
        else if (countSelected == 1)
            countTextView.setText(countSelected + " contact selected");
        else countTextView.setText(countSelected + " contacts selected");
    }

    private void setCountOfSelection(List<StatusPrivacyModel> privacyModelList) {
        Iterator<StatusPrivacyModel> iterator = privacyModelList.iterator();
        while (iterator.hasNext()) {
            StatusPrivacyModel model = iterator.next();
            if (model.isAuthorized())
                countSelected++;
        }
        upDateCountTextView();
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
                if (privacyAdapter.selectAll())
                    countSelected = privacyModelList.size();
                else countSelected = 0;
                upDateCountTextView();
                break;
            case R.id.search_status_privacy:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataBase.close();
    }
}
