package com.example.whatsappclone.ActivityClass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;

import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity {
    private String userUid  =null;
    private String contactName=null;
    private String userPhoneNumber;
    private TextView contactNameView,onLineState;
    private CircleImageView profileImage;
    private RecyclerView recyclerView;
    private ImageButton send,emoji,camera,attachFile;
    private DataBase dataBase;
    private EditText messageEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);
        dataBase=new DataBase(this);
        Intent intent=getIntent();
        userPhoneNumber=intent.getStringExtra("phone_number");
        userUid=intent.getStringExtra("uid");
        contactName=intent.getStringExtra("contact_name");
        contactNameView=toolbar.findViewById(R.id.chat_contact_name);
        contactNameView.setText(contactName);
        onLineState=toolbar.findViewById(R.id.chat_onlineState);
        profileImage=toolbar.findViewById(R.id.chat_profile_image);
        messageEditText=findViewById(R.id.chat_message);
        Glide.with(this)
                .load(dataBase.getUserProfile(userUid,null).getImageUrl())
                .error(R.drawable.ic_default_avatar_profile)
                .into(profileImage);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //TODO:send that your are writing...
                if (s.toString().trim().length() > 0) {

                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_mune,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.phone_call:
                break;
            case R.id.clear_chat:
                break;
            case R.id.chat_search:
                break;
            case R.id.media:
                break;
            case R.id.block:
                break;
            case R.id.mute_notifications:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
