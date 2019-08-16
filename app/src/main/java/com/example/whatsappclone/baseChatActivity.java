package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class baseChatActivity extends AppCompatActivity {
    private FirebaseAuth auth=FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_chat);


    }


    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser()==null) {
            finish();
            startActivity(new Intent(baseChatActivity.this,MainActivity.class));
        }

    }
}
