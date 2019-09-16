package com.example.whatsappclone.AssistanceClass;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.GetMessages;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.WorkEvent;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

public class BackgroundWorker extends Worker {
    Context context;
    private static final String TAG = "BackgroundWorker";

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        /* when the user open chatActivity the other user number will be her
         * so the user will not receive notifications from that number
         * and the chat activity will take care the message store
         */

        final String phone_number = getInputData().getString("phone_number");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference conversation = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("conversation");
        /*ref to chat collection so when any one want to talk to me will send to with collection message
         * with his number and the listener below will catch the number and get the messages from fireStore
         * to data base
         */
        final CollectionReference eventRef = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("event");
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: ", e);
                    return;
                }
                for (DocumentChange documentChange : Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED:
                            WorkEvent event = documentChange.getDocument().toObject(WorkEvent.class);
                            //delete the document from collection
                            eventRef.document(documentChange.getDocument().getId()).delete();
                            Log.d(TAG, "ruing... :");
                            // add the contact phone number to conversation (users who you have conversation with them )
                            Map<String, String> map = new HashMap<>();
                            map.put("phone_number", event.getPhoneNumber());
                            conversation.document(event.getPhoneNumber()).set(map);
                            //add,delete,update DataBase
                            dealWithEvent(event, phone_number);

                            break;
                    }
                }
            }
        });
        return Result.success();
    }

    //add,delete,update DataBase
    public void dealWithEvent(WorkEvent event, String phoneNumberExcludedFromNotifications) {
        DataBase dataBase = new DataBase(context);
        List<String> mutedNumbers = dataBase.getAllMutedConversations();
        String phoneNumber = event.getPhoneNumber();
        if (event.getReadOrDelivered().equals(DataBase.MessageState.DELIVERED))
            dataBase.updateMessageState(phoneNumber, DataBase.MessageState.DELIVERED);
        else if (event.getReadOrDelivered().equals(DataBase.MessageState.READ))
            dataBase.updateMessageState(phoneNumber, DataBase.MessageState.READ);
        else if (event.isDeleteMessage())
            dataBase.deleteMessage(phoneNumber, event.getMessageModel());
        else if (event.isNewMessage()) {
            if (!phoneNumber.equals(phoneNumberExcludedFromNotifications))
                if (!mutedNumbers.contains(phoneNumber)) {
                    //TODO:show an notifications
                }
            dataBase.addMessageInChatTable(event.getPhoneNumber(), event.getMessageModel());
        }
    }
}
