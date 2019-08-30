package com.example.whatsappclone.WhatsAppFireStore;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsApp_Models.ProfileImage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadMedia {
    private Context context;
    private DataBase dataBase;
    private OnUploadCompleteListener onUploadCompleteListener;
    private final String PROFILEFOLDER  = "Profile";
    private final String PROFILEIMAGE  = "Profile";
    private FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    private CollectionReference profilesRef=firestore.collection("profile");
    private DocumentReference profileDocRef=profilesRef.document(UserSettings.PHONENUMBER);
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private StorageReference userstorageReference=firebaseStorage.getReference().child(UserSettings.PHONENUMBER);
    public UploadMedia(Context context) {
         this.context=context;
         this.dataBase=new DataBase(context);
    }

    public void uploadProfileImage(Uri imageUri){
    final StorageReference profileImagestorageReference=userstorageReference.child(PROFILEFOLDER).child(PROFILEIMAGE);
    profileImagestorageReference.delete();
    UploadTask uploadTask= profileImagestorageReference.putFile(imageUri);
        Task<Uri> task=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful())
                    return profileImagestorageReference.getDownloadUrl();
                else throw task.getException();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                    Uri profileUri=task.getResult();
                    String profilePath= profileImagestorageReference.getPath();
                ProfileImage profileImage=new ProfileImage(profilePath,profileUri.toString());
                dataBase.upDateProfileImage(UserSettings.UID,profileImage);
                onUploadCompleteListener.onUploadCompleteListener(profileUri.toString());
                profileDocRef.update("profileImage",profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "can not upload profile image!", Toast.LENGTH_SHORT).show();
            }
        });




    }
    public void OnComplete(OnUploadCompleteListener onUploadCompleteListener){
        this.onUploadCompleteListener=onUploadCompleteListener;
    }
    public interface OnUploadCompleteListener{
        void onUploadCompleteListener(String uri);
    }

}
