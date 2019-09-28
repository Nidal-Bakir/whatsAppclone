package com.example.whatsappclone.WhatsAppFireStore;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.List;

public class UploadMedia {
    private Context context;
    private DataBase dataBase;
    private static final String TAG = "UploadMedia";
    private OnProfileUploadCompleteListener onProfileUploadCompleteListener;
    private OnStatusUploadCompleteListener onStatusUploadCompleteListener;
    private final String PROFILE_FOLDER = "Profile";
    private final String PROFILE_IMAGE = "Profile";
    // root fireStore
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    // Reference for the profiles Collection
    private CollectionReference profilesRef = firestore.collection("profile");
    // user profile document
    private DocumentReference profileDocRef = profilesRef.document(UserSettings.PHONENUMBER);
    // root FireBase Storage
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    //user folder in storage
    private StorageReference userStorageReference = firebaseStorage.getReference().child(UserSettings.PHONENUMBER);

    public UploadMedia(Context context) {
        this.context = context;
        this.dataBase = new DataBase(context);
    }

    public void uploadProfileImage(Uri imageUri) {
        // ref for profile folder => image as name (profile.png/.jpg)
        final StorageReference profileImagestorageReference = userStorageReference.child(PROFILE_FOLDER).child(PROFILE_IMAGE);
        profileImagestorageReference.delete(); //remove the image and replace it
        UploadTask uploadTask = profileImagestorageReference.putFile(imageUri);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            }
        });
        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    return profileImagestorageReference.getDownloadUrl();
                else throw task.getException();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                //image url for download
                Uri profileUri = task.getResult();
                //path of image
                String profilePath = profileImagestorageReference.getPath();
                ProfileImage profileImage = new ProfileImage(profilePath, profileUri.toString());
                //add the data to Table
                dataBase.upDateProfileImage(UserSettings.UID, profileImage);
                onProfileUploadCompleteListener.onUploadCompleteListener(profileUri.toString());
                // upDate the data in fireStore (user profile)
                profileDocRef.update("profileImage", profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "can not upload profile image!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void OnComplete(OnProfileUploadCompleteListener onProfileUploadCompleteListener) {
        this.onProfileUploadCompleteListener = onProfileUploadCompleteListener;
    }

    public interface OnProfileUploadCompleteListener {
        void onUploadCompleteListener(String uri);
    }

    public void upLoadStatusImage(Uri statusUri) {
        // ref for status folder => image as name (status.png/.jpg)
        final StorageReference statusStorageReference = userStorageReference.child("status").child("status");
        statusStorageReference.delete(); //remove the image and replace it
        UploadTask uploadTask = statusStorageReference.putFile(statusUri);
        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    return statusStorageReference.getDownloadUrl();
                else throw task.getException();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Calendar calendar = Calendar.getInstance();
                String uploadTime = String.valueOf(calendar.getTimeInMillis());
                //image url for download
                Uri statusUri = task.getResult();
                //path of image
                String statusPath = statusStorageReference.getPath();
                Status status = new Status(statusPath, statusUri.toString(), uploadTime);
                //add the data to Table
                dataBase.upDateStatusImage(UserSettings.UID, null, status);
                // upDate the data in fireStore (user profile)
                profileDocRef.update("status", status);
                //send the status to users
                sendStatus(status);
                status.setPhone_number(UserSettings.PHONENUMBER);
                onStatusUploadCompleteListener.onUploadCompleteListener(status);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "can not upload status image!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendStatus(Status status) {
        removeStatusFromFireStore();
        List<String> phone_numbers = dataBase.getAllAuthorizedContacts(true);
        String myPhoneNumber = UserSettings.PHONENUMBER;
        CollectionReference statusCollectionReference;
        for (String number : phone_numbers) {
            statusCollectionReference = profilesRef.document(number).collection("status");
            statusCollectionReference.document(myPhoneNumber).set(status);
        }
    }

    public void removeStatusFromFireStore() {
        CollectionReference myStoryVisitRef = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("visitStatus");
        List<VisitStatus> visitStatusList = dataBase.getAllVisits();
        for (VisitStatus visitStatus : visitStatusList)
            myStoryVisitRef.document(visitStatus.getPhone_number()).delete();

        List<String> phone_numbers = dataBase.getAllAuthorizedContacts(false);
        CollectionReference statusCollectionReference;
        String myPhoneNumber = UserSettings.PHONENUMBER;
        for (String number : phone_numbers) {
            statusCollectionReference = profilesRef.document(number).collection("status");
            statusCollectionReference.document(myPhoneNumber).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: "+task.isSuccessful());
                }
            });
        }
    }

    public void OnComplete(OnStatusUploadCompleteListener onStatusUploadCompleteListener) {
        this.onStatusUploadCompleteListener = onStatusUploadCompleteListener;
    }

    public interface OnStatusUploadCompleteListener {
        void onUploadCompleteListener(Status status);
    }

}
