package com.example.whatsappclone.ActivityClass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final int RESULT_CODE = 757;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                UserSettings.createprofilePage();
                    finish();
                    startActivity(new Intent(MainActivity.this, BaseChatActivity2.class));
                }else{
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                  //  showSnackbar(" sign in cancelled! ");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //showSnackbar(" NO NETWORK! ");
                    return;
                }

                //showSnackbar("Sign-in error");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }

        }


    }
    private void showSnackbar(String s){
         Snackbar.make( new View(this).findViewById(R.id.sign_bt),s,Snackbar.LENGTH_LONG).show();
         //Todo: make the snackbar for sign in

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.sign_bt);

        //sign up or log in to whatsApp
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetCheck.isOnline()) {
                    IdpConfig phoneConfigWithDefaultNumber = new AuthUI.IdpConfig.PhoneBuilder()
                            .setDefaultCountryIso("sy")
                            .build();
                    startActivityForResult(AuthUI
                            .getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(phoneConfigWithDefaultNumber))
                            .setTosAndPrivacyPolicyUrls(
                                    "https://joebirch.co/terms.html"
                                    , "https://joebirch.co/privacy.html")
                            .build(), RESULT_CODE);
                }else Toast.makeText(getApplicationContext(), "Failed! Please check  your internet connection.", Toast.LENGTH_SHORT).show();

            }
        });

    }


}
