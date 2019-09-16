package com.example.whatsappclone.ActivityClass;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatActivity extends AppCompatActivity implements  DroidListener {
    private String userUid = null;
    private String contactName = null;
    private String userPhoneNumber;
    private TextView contactNameView, onLineState;
    private CircleImageView profileImage;
    private RecyclerView recyclerView;
    private ImageButton send, emoji, camera, attachFile;
    private DataBase dataBase;
    private EmojiEditText messageEditText;
    private ConstraintLayout messageBox;
    private CoordinatorLayout chatLayoutRoot;
    private EmojiPopup emojiPopup;
    private DroidNet droidNet;

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
        userPhoneNumber = intent.getStringExtra("phone_number");
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
        Glide.with(this)
                .load(dataBase.getUserProfile(userUid, null).getImageUrl())
                .error(R.drawable.ic_default_avatar_profile)
                .into(profileImage);

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
        super.onBackPressed();

    }
}
