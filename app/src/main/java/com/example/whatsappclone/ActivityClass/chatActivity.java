package com.example.whatsappclone.ActivityClass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.whatsappclone.AssistanceClass.BackgroundWorker;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.UserProfile;
import com.example.whatsappclone.WhatsApp_Models.WorkEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

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
        //set profile image for the user
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
        // cancelAllWorkers and start new one and send other user phone number for denied the notifications
        WorkManager.getInstance(getBaseContext()).cancelAllWorkByTag("messagesListener");
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType
                .CONNECTED).build();
        Data data = new Data.Builder()
                .putString("phone_number", otherUserPhoneNumberThisChat)
                .build();
        OneTimeWorkRequest worker = new OneTimeWorkRequest
                .Builder(BackgroundWorker.class)
                .setInputData(data)
                .setConstraints(constraints)
                .addTag("messagesListener")
                .build();
        WorkManager.getInstance(getBaseContext()).enqueue(worker);

        dataBase.chatTableListener(new DataBase.ChatTableListener() {
            @Override
            public void onAddNewMessage(MessageModel messageModel, DataBase.Conversation conversation) {
                if (messageModel.getPhoneNumber().equals(otherUserPhoneNumberThisChat) || messageModel.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
                    WorkEvent workEvent = new WorkEvent(UserSettings.PHONENUMBER, DataBase.MessageState.READ, false, false, null);
                    fireStore.collection("profile")
                            .document(otherUserPhoneNumberThisChat).collection("event").add(workEvent);
                }
            }

            @Override
            public void onChangeMessageState(String otherUserPhoneNumber) {
                if (otherUserPhoneNumber.equals(otherUserPhoneNumberThisChat)) {

                }
            }

            @Override
            public void onDeleteMessage(String otherUserPhoneNumber, MessageModel messageModel) {
                if (otherUserPhoneNumber.equals(otherUserPhoneNumberThisChat)) {

                }
            }
        });
        UpdateOtherUserMessageState otherUserMessageState = new UpdateOtherUserMessageState(this, otherUserPhoneNumberThisChat);
        otherUserMessageState.execute();
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected)
            dataBase.updateAllHoledMessages();
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

    // up date the message state for local dataBase and FireStore
    class UpdateOtherUserMessageState extends AsyncTask<Void, Void, Void> {
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
            Bundle bundle = dataBase.getLastMessage(otherPhoneNumber);
            if (!bundle.getBoolean("isMyMessage") && bundle.getInt("messageState") != DataBase.READ) {
                List<String> uidMessages = dataBase.getAllOtherUserMessagesMarkedAsDELIVERED(otherPhoneNumber);
                //tell the other user that you are Read his message and update the message state on fire store
                // for your copy of chat and other user.
                for (String messageUid : uidMessages) {
                    fireStore.collection("messages").document(otherPhoneNumber)
                            .collection(UserSettings.PHONENUMBER).document(messageUid).update("messageState", DataBase.READ);
                    fireStore.collection("messages").document(UserSettings.PHONENUMBER)
                            .collection(otherPhoneNumber).document(messageUid).update("messageState", DataBase.READ);
                    workEvent = new WorkEvent(UserSettings.PHONENUMBER, DataBase.MessageState.READ, false, false, null);
                    fireStore.collection("profile")
                            .document(otherPhoneNumber).collection("event").add(workEvent);
                }
            }
            return null;
        }
    }
}
