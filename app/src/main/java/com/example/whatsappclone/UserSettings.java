package com.example.whatsappclone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class UserSettings {
    private FirebaseAuth firebaseAuth;

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
        //String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

    public static String getUserUID() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


}

