package com.example.whatsappclone.ActivityClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.whatsappclone.Adapters.ChatAdapter;
import com.example.whatsappclone.NotificationsPackage.NotificationClass;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.MessagesPackage.Message;
import com.example.whatsappclone.WhatsApp_Models.UserProfile;
import com.example.whatsappclone.WhatsApp_Models.WorkEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

//implements  DroidListener
public class chatActivity extends AppCompatActivity implements DroidListener {
    private String userUid = null;
    private String contactName = null;
    private String otherUserPhoneNumberThisChat = null;
    private String profileImageURL = null;
    private CircleImageView profileImage;
    private TextView contactNameView, onLineState;
    private RecyclerView recyclerView;
    private ImageButton send, emoji, camera, attachFile;
    private DataBase dataBase;
    private EmojiEditText messageEditText;
    private ConstraintLayout messageBox;
    private CoordinatorLayout chatLayoutRoot;
    private EmojiPopup emojiPopup;
    private DroidNet droidNet;
    private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    private CollectionReference profilesCollection = fireStore.collection("profile");
    private NotificationClass notificationClass;


    // for send button
    private enum SendState {TEXT, VOICE}

    SendState sendState = SendState.VOICE;

    private static final String TAG = "chatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // emoji init and select the emoji package from settings
        SharedPreferences emojiType = PreferenceManager.getDefaultSharedPreferences(this);
        String type = emojiType.getString("emoji", "ios");
        if (type != null)
            switch (type) {
                case "twitter":
                    EmojiManager.install(new TwitterEmojiProvider());
                    break;
                case "google":
                    EmojiManager.install(new GoogleEmojiProvider());
                    break;
                default://ios is the default emoji type
                    EmojiManager.install(new IosEmojiProvider());
                    break;
            }
        else EmojiManager.install(new IosEmojiProvider());
        setContentView(R.layout.activity_chat);
        notificationClass = new NotificationClass(this);

        // for listening for network connection state and Internet connectivity
        DroidNet.init(this);
        droidNet = DroidNet.getInstance();
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        setTitle(null);
        dataBase = new DataBase(this);
        Intent intent = getIntent();
        otherUserPhoneNumberThisChat = intent.getStringExtra("phone_number");
        userUid = intent.getStringExtra("uid");
        contactName = intent.getStringExtra("contact_name");
        //init the Views
        contactNameView = toolbar.findViewById(R.id.chat_contact_name);
        contactNameView.setText(contactName);
        onLineState = toolbar.findViewById(R.id.chat_onlineState);
        profileImage = toolbar.findViewById(R.id.chat_profile_image);
        chatLayoutRoot = findViewById(R.id.chat_Root);
        messageEditText = findViewById(R.id.chat_message);
        // init the popupEmoji
        emojiPopup = EmojiPopup.Builder.fromRootView(chatLayoutRoot).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                emoji.setImageResource(R.drawable.ic_emojis);
            }
        }).build(messageEditText);
        messageBox = findViewById(R.id.chat_Box_container);
        camera = findViewById(R.id.chat_camera);
        attachFile = findViewById(R.id.attach);
        emoji = findViewById(R.id.chat_emoji);
        send = findViewById(R.id.chat_send);
        recyclerView = findViewById(R.id.chat_recycler_view);
        // delete the stored Notification for this user
        dataBase.deleteNotificationForUser(otherUserPhoneNumberThisChat);
        //set profile image for the user
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        if (contactName.equals(otherUserPhoneNumberThisChat)) {
            // Anonymous number get the profile image from his Profile on fireStore
            profilesCollection.document(otherUserPhoneNumberThisChat).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        UserProfile userProfile = task.getResult().toObject(UserProfile.class);
                        userUid = userProfile.getUid();
                        profileImageURL = userProfile.getProfileImage().getImageUrl();
                        Glide.with(getApplicationContext())
                                .load(profileImageURL)
                                .placeholder(R.color.white)
                                .error(R.drawable.ic_default_avatar_profile)
                                .into(profileImage);
                    }
                }
            });
        } else {
            profileImageURL = dataBase.getUserProfile(userUid, null).getImageUrl();
            Glide.with(this)
                    .load(profileImageURL)
                    .error(R.drawable.ic_default_avatar_profile)
                    .into(profileImage);
        }
        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                    emoji.setImageResource(R.drawable.ic_emojis);
                }
            }
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //TODO:send that your are writing...
                ConstraintSet messageViewConstraint = new ConstraintSet();
                messageViewConstraint.clone(messageBox);
                final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP
                        , 50, getApplicationContext().getResources().getDisplayMetrics());
                if (s.toString().trim().length() > 0) {
                    if (sendState != SendState.TEXT) {
                        sendState = SendState.TEXT;
                        attachFile.animate().translationY(px).setDuration(150).start();
                        camera.animate().translationY(px).setDuration(150).start();
                        messageViewConstraint.connect(R.id.chat_message, ConstraintSet.END, R.id.chat_Box_container, ConstraintSet.END, 32);
                        messageViewConstraint.applyTo(messageBox);
                        send.animate().scaleX(0).scaleY(0).setDuration(100).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                send.setImageResource(R.drawable.ic_chat_send);
                                send.animate().scaleX(1).scaleY(1).setDuration(100).start();
                            }
                        }).start();
                    }
                } else if (sendState != SendState.VOICE) {
                    sendState = SendState.VOICE;
                    attachFile.animate().translationY(0).setDuration(100).start();
                    camera.animate().translationY(0).setDuration(100).start();
                    messageViewConstraint.connect(R.id.chat_message, ConstraintSet.END, R.id.attach, ConstraintSet.START, 0);
                    messageViewConstraint.applyTo(messageBox);
                    send.animate().scaleX(0).scaleY(0).setDuration(100).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            send.setImageResource(R.drawable.ic_voice);
                            send.animate().scaleX(1).scaleY(1).setDuration(100).start();
                        }
                    }).start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // show OR dismiss the popup emoji layout
        emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup.isShowing()) {
                    emoji.setImageResource(R.drawable.ic_emojis);
                } else {
                    emoji.setImageResource(R.drawable.ic_keyboard);
                }
                emojiPopup.toggle();
            }
        });

        // set up the recycler view for chat
        List<Message> messageList = dataBase.getChatMessages(otherUserPhoneNumberThisChat, 50);
        final ChatAdapter chatAdapter = new ChatAdapter(this, messageList);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        if (chatAdapter.getItemCount() > 15)
            linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                if (chatAdapter.getItemCount() > 15)
                    linearLayoutManager.setStackFromEnd(true);

            }
        });
        recyclerView.setAdapter(chatAdapter);

        // listener for data base add,update and delete
        dataBase.chatTableListener(new DataBase.ChatTableListener() {
            @Override
            public void onAddNewMessage(MessageModel messageModel, DataBase.Conversation conversation) {
                if (messageModel.getPhoneNumber().equals(otherUserPhoneNumberThisChat) || messageModel.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
                    //if the message from other use then tell him thar u are read his message
                    if (messageModel.getPhoneNumber().equals(otherUserPhoneNumberThisChat)) {
                        WorkEvent workEvent = new WorkEvent(UserSettings.PHONENUMBER, DataBase.MessageState.READ, false, false, messageModel);
                        fireStore.collection("profile")
                                .document(otherUserPhoneNumberThisChat).collection("event").add(workEvent);

                    }
                    // add the message to the adapter
                    chatAdapter.addMessage(messageModel);
                }
            }

            @Override
            public void onChangeMessageState(String otherUserPhoneNumber, String messageUid, DataBase.MessageState messageState) {
                if (otherUserPhoneNumber.equals(otherUserPhoneNumberThisChat)) {
                    // update your message in adapter based on message uid
                    chatAdapter.updateMessage(messageUid, messageState);
                }
            }

            @Override
            public void onDeleteMessage(String otherUserPhoneNumber, MessageModel messageModel) {
                if (otherUserPhoneNumber.equals(otherUserPhoneNumberThisChat)) {
                    // update your message in adapter based on message uid
                    chatAdapter.updateMessage(messageModel.getMessageUid(), DataBase.MessageState.MESSAGE_DELETED);
                }
            }
        });
        // attach the listener for messages
        eventListener();

        // tell the other user that your are read his messages
        UpdateOtherUserMessageState otherUserMessageState = new UpdateOtherUserMessageState(this, otherUserPhoneNumberThisChat);
        otherUserMessageState.execute();

        // send button listener
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendState == SendState.TEXT) {
                    Calendar calendar = Calendar.getInstance();
                    String textMessage = String.valueOf(messageEditText.getText());
                    MessageModel messageModel = new MessageModel(UserSettings.PHONENUMBER
                            , fireStore.collection("messages").document().getId()
                            , textMessage
                            , null
                            , null
                            , null
                            , null
                            , String.valueOf(calendar.getTimeInMillis()));
                    messageEditText.setText("");
                    dataBase.addMessageInChatTable(otherUserPhoneNumberThisChat, messageModel, false);
                } else
                    Toast.makeText(chatActivity.this, "click and hold to record your voice", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void eventListener() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final CollectionReference conversation = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("conversation");
        /*ref to chat collection so when any one want to talk to me will send to with collection message
         * with his number and the listener below will catch the number and get the messages from fireStore
         * to data base
         */
        final CollectionReference eventRef = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("event");
        eventRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
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
                            dealWithEvent(event);

                            break;
                        case MODIFIED:
                            Log.d(TAG, "onEvent: " + "mod");
                            break;
                        case REMOVED:
                            Log.d(TAG, "onEvent: removed");
                            break;
                    }
                }
            }
        });
    }

    //add,delete,update DataBase
    public void dealWithEvent(final WorkEvent event) {

        List<String> mutedNumbers = dataBase.getAllMutedConversations();
        String phoneNumber = event.getPhoneNumber();
        if (event.getReadOrDelivered().equals(DataBase.MessageState.DELIVERED))
            dataBase.updateMessageState(phoneNumber, DataBase.MessageState.DELIVERED, event.getMessageModel().getMessageUid(), false);
        else if (event.getReadOrDelivered().equals(DataBase.MessageState.READ))
            dataBase.updateMessageState(phoneNumber, DataBase.MessageState.READ, event.getMessageModel().getMessageUid(), false);
        else if (event.isDeleteMessage())
            dataBase.deleteMessage(phoneNumber, event.getMessageModel(), false);
        else if (event.isNewMessage()) {
            if (!phoneNumber.equals(otherUserPhoneNumberThisChat))
                if (!mutedNumbers.contains(phoneNumber)) {
                    // show notification
                    if (dataBase.pushNotificationInDataBase(event.getMessageModel())) {
                        notificationClass.notifyUser();
                    }
                }
            dataBase.addMessageInChatTable(event.getPhoneNumber(), event.getMessageModel(), false);

        }
    }


    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            dataBase.updateAllHoledMessages();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_mune, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        droidNet.removeInternetConnectivityChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        dataBase.reSetMessageCount(otherUserPhoneNumberThisChat);
        super.onBackPressed();

    }

    @Override
    protected void onStart() {
        super.onStart();
        eventListener();
    }

    // up date the message state for local dataBase and FireStore
    private static class UpdateOtherUserMessageState extends AsyncTask<Void, Void, Void> {
        private DataBase dataBase;
        private String otherPhoneNumber;
        private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();

        public UpdateOtherUserMessageState(Context context, String otherPhoneNumber) {
            this.otherPhoneNumber = otherPhoneNumber;
            dataBase = new DataBase(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            WorkEvent workEvent;

            List<String> uidMessages = dataBase.getAllOtherUserMessagesMarkedAsDELIVERED(otherPhoneNumber);
            //tell the other user that you are Read his message and update the message state on fire store
            // for your copy of chat and other user.
            for (String messageUid : uidMessages) {
                fireStore.collection("messages").document(otherPhoneNumber)
                        .collection(UserSettings.PHONENUMBER).document(messageUid).update("messageState", DataBase.READ);
                fireStore.collection("messages").document(UserSettings.PHONENUMBER)
                        .collection(otherPhoneNumber).document(messageUid).update("messageState", DataBase.READ);
                workEvent = new WorkEvent(UserSettings.PHONENUMBER, DataBase.MessageState.READ, false, false, new MessageModel(null, messageUid, null, null, null, null, null, null));
                fireStore.collection("profile")
                        .document(otherPhoneNumber).collection("event").add(workEvent);
            }

            return null;
        }
    }
}
