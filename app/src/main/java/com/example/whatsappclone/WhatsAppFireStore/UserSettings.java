package com.example.whatsappclone.WhatsAppFireStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.whatsappclone.MainActivity;
import com.example.whatsappclone.WhatsApp_Models.UserProfile;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class UserSettings {
    private static final String TAG = "UserSettings";

    private FirebaseAuth firebaseAuth;
    public static String PHONENUMBER = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    public static String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final String PROFILE = "profile";

    public UserSettings() {
        firebaseAuth = FirebaseAuth.getInstance();

    }

    public static Boolean isSignIn() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        return firebaseAuth.getCurrentUser() == null ? false : true;
    }

    public static void signOut(final Activity activity, Context context) {
        AuthUI.getInstance().signOut(context).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
        });
    }

    public static void deleteUser(final Activity activity, final Context context) {

        AuthUI.getInstance().delete(context).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //ToDO:delete the user chats and all subCollections
                    activity.startActivity(new Intent(activity, MainActivity.class));
                    activity.finish();
                }

            }
        });

    }



     public static void createprofilePage() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        PHONENUMBER = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserProfile userProfile = new UserProfile("online", PHONENUMBER, UID, null, null);
        firestore.collection(PROFILE).document(PHONENUMBER).set(userProfile, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: Failed to create profile page",e );
            }
        });




    }



}

