package com.example.whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth=FirebaseAuth.getInstance();
    private static final int RESULT_CODE = 757;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.sign_bt);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               IdpConfig phoneConfigWithDefaultNumber = new AuthUI.IdpConfig.PhoneBuilder()
                        .setDefaultCountryIso("sy")
                        .build();
              startActivityForResult(AuthUI
                      .getInstance()
                      .createSignInIntentBuilder()
                      .setAvailableProviders(Arrays.asList(phoneConfigWithDefaultNumber))
                      .setTosAndPrivacyPolicyUrls(
                        "https://joebirch.co/terms.html"
                              ,"https://joebirch.co/privacy.html")
                      .build(),RESULT_CODE);
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser()!=null) {
            finish();
            startActivity(new Intent(MainActivity.this,baseChatActivity.class));
        }

    }


}
