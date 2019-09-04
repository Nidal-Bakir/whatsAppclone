package com.example.whatsappclone.ActivityClass;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
     LinearLayout loading;
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
            } else {
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.sign_bt);
         loading = findViewById(R.id.loading);
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final String countryCode = tm.getSimCountryIso();
        //sign up or log in to whatsApp
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                if (InternetCheck.isOnline()) {
                    IdpConfig phoneConfigWithDefaultNumber = new AuthUI.IdpConfig.PhoneBuilder()
                            .setDefaultCountryIso(countryCode)
                            .build();
                    startActivityForResult(AuthUI
                            .getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(phoneConfigWithDefaultNumber))
                            .setTosAndPrivacyPolicyUrls(
                                    "https://joebirch.co/terms.html"
                                    , "https://joebirch.co/privacy.html")
                            .build(), RESULT_CODE);

                } else {
                    loading.setVisibility(View.GONE);
                    View view = findViewById(R.id.sign_bt);
                    final Snackbar snackbar = Snackbar.make(view, "no internet connection!", Snackbar.LENGTH_LONG);
                    //Toast.makeText(getApplicationContext(), "Failed! Please check  your internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loading.setVisibility(View.GONE);
    }
}
