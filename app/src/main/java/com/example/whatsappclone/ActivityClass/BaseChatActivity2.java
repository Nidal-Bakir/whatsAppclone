package com.example.whatsappclone.ActivityClass;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;


import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.example.whatsappclone.Adapters.ConversationAdapter;
import com.example.whatsappclone.Adapters.StatusAdapter;
import com.example.whatsappclone.AssistanceClass.InternetCheck;
import com.example.whatsappclone.AssistanceClass.OnSwipeListener;
import com.example.whatsappclone.AssistanceClass.BackgroundWorker;
import com.example.whatsappclone.NotificationsPackage.NotificationClass;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.SyncContactsWithCloudDB;
import com.example.whatsappclone.WhatsAppFireStore.UploadMedia;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.MessageModel;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;
import com.example.whatsappclone.WhatsApp_Models.WorkEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import io.fabric.sdk.android.Fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class BaseChatActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DroidListener, DataBase.ChatTableListener {
    private static final String TAG = "BaseChatActivity2";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    public static boolean STOP_WORKER = true;
    private static final int IMAGE_CHOOSER_REQUEST_CODE = 476;
    private static final int ADD_STATUS_REQUEST_CODE = 823;
    private static final int PERMISSIONS = 1234;
    private String[] appPermissions = {Manifest.permission.READ_CONTACTS
            , Manifest.permission.RECORD_AUDIO
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.CAMERA
            , Manifest.permission.SEND_SMS
            , Manifest.permission.ACCESS_FINE_LOCATION};
    private TextView profilePhoneNumber;
    private CircleImageView profileImage;
    private UploadMedia uploadMedia;
    private List<Status> statusList;
    private DataBase dataBase;
    private ProgressBar profileProgressBar;
    private RecyclerView statusRecyclerView;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    // for attach listener on status collection
    private CollectionReference statusCollectionRef;
    //to send message that the user has visited his/her story
    private CollectionReference storyVisitRef;
    // to attach listener on Visit status collection then i know how visited my story
    private CollectionReference myStoryVisitRef;
    private StatusAdapter statusAdapter;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction;
    private SyncContactsWithCloudDB contactsWithCloudDB;
    private String countryCode;
    private DroidNet droidNet;
    ConstraintLayout noConversation;
    private RecyclerView conversationRecyclerView;
    private ConversationAdapter conversationAdapter;
    private OneTimeWorkRequest uploadWork;
    private NotificationClass notificationClass;
    public static boolean isConnected = false;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            int denidCount = 0;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    denidCount++;
            }
            if (denidCount == 0)
                startApp();
            else {
                View view = findViewById(R.id.chat_floating_bt);
                final Snackbar snackbar = Snackbar.make(view, "The app need all this permissions!!", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Re Ask me", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkAndRequestPermissions();
                    }
                });

                snackbar.show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_CHOOSER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    profileProgressBar.setVisibility(View.VISIBLE);
                    if (data == null) {
                        profileProgressBar.setVisibility(View.GONE);
                        return;
                    }
                    //check if the user connect to the internet
                    InternetCheck internetCheck = new InternetCheck(this);
                    internetCheck.execute();
                    // this method will called when the check Completed because the check can not run
                    // on UI THREAD
                    internetCheck.onComplete(new InternetCheck.OnCheckComplete() {
                        @Override
                        public void onCheckComplete(boolean isOnline) {
                            if (isOnline) {
                                uploadMedia.uploadProfileImage(data.getData());
                                uploadMedia.OnComplete(new UploadMedia.OnProfileUploadCompleteListener() {
                                    @Override
                                    public void onUploadCompleteListener(final String uri) {
                                        String oldimaeg = getSharedPreferences("oldImage", MODE_PRIVATE).getString("image", "");
                                        RequestBuilder<Drawable> requestBuilder = Glide.with(getApplicationContext()).load(oldimaeg);
                                        Glide.with(getApplicationContext())
                                                .load(uri)
                                                .placeholder(R.color.white)
                                                .error(requestBuilder)//set the default image if the user delete the profile image or something  went wrong
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        profileProgressBar.setVisibility(View.GONE);
                                                        Toast.makeText(BaseChatActivity2.this, "Can't update your profile image ", Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                        getSharedPreferences("oldImage", MODE_PRIVATE).edit().putString("image", uri).apply();
                                                        //hide the progressBar
                                                        profileProgressBar.setVisibility(View.GONE);
                                                        statusAdapter.onProfileImageChange();
                                                        Toast.makeText(BaseChatActivity2.this, "your profile image has been updated ", Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                })
                                                .into(profileImage);


                                    }
                                });
                            } else {
                                profileProgressBar.setVisibility(View.GONE);
                                //show an SnackBar to tell the user what want wrong
                                showSnackBar();
                            }
                        }
                    });
                }
                break;
            case ADD_STATUS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data == null)
                        return;
                    //check if the user connect to the internet
                    InternetCheck internetCheck = new InternetCheck(this);
                    internetCheck.execute();
                    // this method will called when the check Completed because the check can not run
                    // on UI THREAD
                    internetCheck.onComplete(new InternetCheck.OnCheckComplete() {
                        @Override
                        public void onCheckComplete(boolean isOnline) {
                            if (isOnline) {
                                //to show the user that the status upload is in progress
                                Status myStatus = new Status("whatever", data.getData().toString(), "whatever");
                                myStatus.setShowProgressBar(true);
                                myStatus.setPhone_number(UserSettings.PHONENUMBER);
                                statusAdapter.addStatusToList(myStatus);
                                //upload the image to FireBase and send the status
                                uploadMedia.upLoadStatusImage(data.getData());
                                uploadMedia.OnComplete(new UploadMedia.OnStatusUploadCompleteListener() {
                                    @Override
                                    public void onUploadCompleteListener(Status status) {
                                        statusAdapter.addStatusToList(status);
                                        dataBase.deleteAllVisits();
                                    }
                                });
                            } else {
                                //show an SnackBar to tell the user what want wrong
                                showSnackBar();
                            }
                        }
                    });

                }
                break;
        }
        statusCollectionListener();
        eventListener();
    }

    private void showSnackBar() {
        View view = findViewById(R.id.chat_floating_bt);
        final Snackbar snackbar = Snackbar.make(view, "no internet connection!", Snackbar.LENGTH_LONG);
        snackbar.setAction("close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
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
        setContentView(R.layout.activity_base_chat2);
        noConversation = findViewById(R.id.no_chat_image);
        // open the login activity
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
            finish();
        } else {
            WorkManager.getInstance(getBaseContext()).cancelAllWorkByTag("messagesListener");
            getSharedPreferences("firstTime", MODE_PRIVATE).edit().putBoolean("firstTime", false).apply();
            // ask for all Permissions
            if (checkAndRequestPermissions()) {
                //i.g US this will use with libphonenumber lib
                // to handle the numbers whose  doesn't have area code i.g(+1)4
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                countryCode = tm.getSimCountryIso();
                notificationClass = new NotificationClass(this);
                startApp();
                dataBase.chatTableListener(this);


            }
        }
    }


    private void startApp() {

        // for listening for network connection state and Internet connectivity
        droidNet = DroidNet.getInstance();
        // inti class for upload media images and videos
        uploadMedia = new UploadMedia(this);
        statusCollectionRef = firestore.collection("profile").document(UserSettings.PHONENUMBER).collection("status");
        // for DataBase Debug
        Stetho.initializeWithDefaults(this);
        //connect to DataBase
        dataBase = new DataBase(this);
        //sync the contacts
        contactsWithCloudDB = new SyncContactsWithCloudDB(getApplicationContext(), countryCode);
        contactsWithCloudDB.execute(false);
        InternetCheck internetCheck = new InternetCheck(this);
        internetCheck.execute();
        internetCheck.onComplete(new InternetCheck.OnCheckComplete() {
            @Override
            public void onCheckComplete(boolean isOnline) {
                BaseChatActivity2.isConnected = isOnline;
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("WhatsApp");
        //open the contacts activity
        final FloatingActionButton fab = findViewById(R.id.chat_floating_bt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BaseChatActivity2.this, ContactsActivity.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View nav = navigationView.getHeaderView(0);
        profilePhoneNumber = nav.findViewById(R.id.profile_phone_number);

        try {// set phone number to the text view in nav_bar
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber myNumber = phoneNumberUtil.parse(UserSettings.PHONENUMBER, countryCode.toUpperCase());
            profilePhoneNumber.setText(phoneNumberUtil.format(myNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
        } catch (NumberParseException e) {
            e.printStackTrace();
            profilePhoneNumber.setText(UserSettings.PHONENUMBER);
        }
        profileProgressBar = nav.findViewById(R.id.profileImageProgressBar);
        /* change progress Bar color  */
        Drawable drawable = profileProgressBar.getIndeterminateDrawable().mutate();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        profileProgressBar.setProgressDrawable(drawable);
        profileImage = nav.findViewById(R.id.profile_Image);

        //choose image profile
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "select avatar"), IMAGE_CHOOSER_REQUEST_CODE);

            }
        });
        contactsWithCloudDB.setOnSyncFinish(new SyncContactsWithCloudDB.OnSyncFinish() {
            @Override
            public void onFinish(List<DataBase.Contact_Profile> contact_profiles) {
                /* set the profile image from DB
                 * and if something went wrong then restore get the old profile image
                 */
                String oldimaeg = getSharedPreferences("oldImage", MODE_PRIVATE).getString("image", "");
                RequestBuilder<Drawable> requestBuilder = Glide.with(getApplicationContext()).load(oldimaeg);
                if (oldimaeg.equals(""))
                    Glide.with(getApplicationContext())
                            .load(dataBase.getUserProfile(UserSettings.UID, null).getImageUrl())
                            .error(R.drawable.ic_default_avatar_profile)//set the default image if the user delete the profile image or something  went wrong
                            .into(profileImage);
                else Glide.with(getApplicationContext())
                        .load(dataBase.getUserProfile(UserSettings.UID, null).getImageUrl())
                        .error(requestBuilder)//set the default image if the user delete the profile image or something  went wrong
                        .into(profileImage);
                // status init
                statusInit();
                //conversation RecyclerView
                conversationRecyclerView = findViewById(R.id.conversationRecycler);
                conversationRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
                List<DataBase.Conversation> conversationList = dataBase.getAllConversation();
                if (conversationList.isEmpty())
                    noConversation.setVisibility(View.VISIBLE);
                else noConversation.setVisibility(View.GONE);
                conversationAdapter = new ConversationAdapter(getApplicationContext(), conversationList);
                conversationRecyclerView.setAdapter(conversationAdapter);
                conversationAdapter.onConversationItemClickListener(new ConversationAdapter.OnConversationItemClickListener() {
                    @Override
                    public void onClick(DataBase.Contact contact, String phoneNumber) {
                        Intent intent = new Intent(BaseChatActivity2.this, chatActivity.class);
                        // registration.remove();
                        if (contact != null) {
                            intent.putExtra("phone_number", contact.getPhone_number());
                            intent.putExtra("uid", contact.getUID());
                            intent.putExtra("contact_name", contact.getContact_name());
                        } else {
                            intent.putExtra("phone_number", phoneNumber);
                            intent.putExtra("uid", "");
                            intent.putExtra("contact_name", phoneNumber);
                        }
                        startActivity(intent);
                    }
                });


            }
        });
        droidNet.addInternetConnectivityListener(BaseChatActivity2.this);
    }

    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(permission);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS);
            return false;
        }
        return true;
    }

    public void statusInit() {
        statusList = dataBase.getAllStatus(); //get all status from DataBase
        statusRecyclerView = findViewById(R.id.StatusRecyclerView);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        statusAdapter = new StatusAdapter(this, this, statusList);
        statusRecyclerView.setAdapter(statusAdapter);
        // when user click add status item
        statusAdapter.onAddStatus(new StatusAdapter.OnAddStatusListener() {
            @Override
            public void onAddStatusListener() {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "select avatar"), ADD_STATUS_REQUEST_CODE);

            }
        });
        // when user click on normal status item
        statusAdapter.onStatusClick(new StatusAdapter.OnStatusItemClickListener() {
            @Override
            public void onStatusItemClickListener(final Status status) {
                StatusViewer statusViewer = StatusViewer.newInstance(status);
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack("viewer");
                fragmentTransaction.add(R.id.drawer_layout, statusViewer);
                fragmentTransaction.commit();
                statusViewer.onActionHandler(new StatusViewer.OnFragmentInteractionListener() {
                    @Override
                    public void onFragmentIGestureDetector(CardView viewedList, OnSwipeListener.Direction direction) {
                        if (direction == OnSwipeListener.Direction.up)
                            //show the list of contacts hows visited the user store
                            viewedList.animate().translationY(0).setDuration(250).start();
                        else {
                            // from DP to PX
                            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, getApplicationContext().getResources().getDisplayMetrics());
                            // hide the list
                            viewedList.animate().translationY(px).setDuration(200).start();
                        }
                    }

                    @Override
                    public void onDeleteButtonClickListener() {
                        onBackPressed();// to close the fragment
                        uploadMedia.removeStatusFromFireStore();
                        statusAdapter.removeStatusFromList(UserSettings.PHONENUMBER);
                        dataBase.deleteAllVisits();
                    }
                });
                // send notification that the user has open the status
                if (!status.getPhone_number().equals(UserSettings.PHONENUMBER)) {
                    final String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                    /*get the other user story and compare the uploading time if it's the same
                     * then send message that you are visit his/her story
                     * */
                    statusCollectionRef.document(status.getPhone_number())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Status otherUserStatus = task.getResult().toObject(Status.class);
                                assert otherUserStatus != null;
                                if (otherUserStatus.getDate().equals(status.getDate()))
                                    storyVisitRef = firestore.collection("profile")
                                            .document(status.getPhone_number()).collection("visitStatus");
                                //send the message
                                storyVisitRef.document(UserSettings.PHONENUMBER).set(new VisitStatus(time));

                            }
                        }
                    });
                }
            }

        });

        statusCollectionListener();
        eventListener();
    }

    ListenerRegistration registration = new ListenerRegistration() {
        @Override
        public void remove() {

        }
    };

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
        registration = eventRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
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
            if (!mutedNumbers.contains(phoneNumber) && !event.getPhoneNumber().equals(UserSettings.PHONENUMBER)) {
                // show notification
                if (dataBase.pushNotificationInDataBase(event.getMessageModel())) {
                    notificationClass.notifyUser();
                }

            }
            dataBase.addMessageInChatTable(event.getPhoneNumber(), event.getMessageModel(), false);

        }
    }

    public void statusCollectionListener() {
        //start listening for changing on status collection
        statusCollectionRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                String phone_number;//it is the same of (get collection id)
                Status status;
                for (DocumentChange documentChange : Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED:
                            phone_number = documentChange.getDocument().getId();
                            status = documentChange.getDocument().toObject(Status.class);
                            status.setPhone_number(phone_number);
                            // check the number
                            if (dataBase.isNumberAFriend(phone_number)) {
                                statusAdapter.addStatusToList(status);
                                dataBase.upDateStatusImage(null, phone_number, status);
                            }
                            break;
                        case REMOVED:
                            phone_number = documentChange.getDocument().getId();
                            if (dataBase.isNumberAFriend(phone_number)) {
                                statusAdapter.removeStatusFromList(phone_number);
                                dataBase.upDateStatusImage(null, phone_number, new Status("", "", ""));
                            }
                            break;
                        case MODIFIED:
                            phone_number = documentChange.getDocument().getId();
                            if (dataBase.isNumberAFriend(phone_number)) {
                                status = documentChange.getDocument().toObject(Status.class);
                                status.setPhone_number(phone_number);
                                dataBase.upDateStatusImage(null, phone_number, status);
                                statusAdapter.addStatusToList(status);
                            }
                            break;
                    }
                }
            }
        });
        //start listening for changing on [visit] status collection
        myStoryVisitRef = firestore.collection("profile")
                .document(UserSettings.PHONENUMBER).collection("visitStatus");
        myStoryVisitRef.orderBy("time", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "onEvent: ", e);
                    return;
                }
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    switch (documentChange.getType()) {
                        case ADDED:
                            VisitStatus visitStatus = documentChange.getDocument().toObject(VisitStatus.class);
                            visitStatus.setPhone_number(documentChange.getDocument().getId());
                            dataBase.addVisit(visitStatus);
                            break;
                    }
                }

            }
        });

    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected)
            dataBase.updateAllHoledMessages();
        BaseChatActivity2.isConnected = isConnected;
        Log.d(TAG, "onInternetConnectivityChanged: " + isConnected);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!getSharedPreferences("firstTime", MODE_PRIVATE).getBoolean("firstTime", true)) {
            //start the background service
            WorkManager.getInstance(getBaseContext()).cancelAllWorkByTag("messagesListener");
            Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType
                    .CONNECTED).build();
            uploadWork = new OneTimeWorkRequest.Builder(BackgroundWorker.class)
                    .setConstraints(constraints).addTag("messagesListener").build();
            getSharedPreferences("work", MODE_PRIVATE).edit().putString("work", uploadWork.getId().toString()).apply();
            WorkManager.getInstance(getBaseContext()).enqueue(uploadWork);
            STOP_WORKER = false;
            contactsWithCloudDB.cancel(true);
            droidNet.removeInternetConnectivityChangeListener(this);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        WorkManager.getInstance(getBaseContext()).cancelAllWorkByTag("messagesListener");
        STOP_WORKER = true;
        eventListener();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        droidNet.addInternetConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        droidNet.removeInternetConnectivityChangeListener(this);
        registration.remove();


    }

    @Override
    protected void onStop() {
        super.onStop();
        registration.remove();
    }


    @Override
    protected void onResume() {
        if (conversationAdapter != null) {
            List<DataBase.Conversation> conversationList = dataBase.getAllConversation();
            conversationAdapter.changeDataSet(conversationList);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragmentManager != null) {
            if (fragmentManager.getBackStackEntryCount() > 0)
                fragmentManager.popBackStackImmediate();
            else {
                dataBase.close();
                super.onBackPressed();
            }
        } else {
            dataBase.close();
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_chat_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks.
        switch (item.getItemId()) {
            case R.id.nav_newGroup:
                //ToDO:handel the new group action
                break;
            case R.id.status_privacy:
                startActivity(new Intent(BaseChatActivity2.this, StatusPrivacy.class));
                break;
            case R.id.nav_contacts:
                startActivity(new Intent(BaseChatActivity2.this, ContactsActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(BaseChatActivity2.this, SettingsActivity.class));

                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;


        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (fragmentManager != null) {
            if (fragmentManager.getBackStackEntryCount() == 0)
                drawer.closeDrawer(GravityCompat.START);
        } else drawer.closeDrawer(GravityCompat.START);
        return false;
    }


    @Override
    public void onAddNewMessage(MessageModel messageModel, DataBase.Conversation conversation) {
        if (conversationAdapter != null)
            conversationAdapter.addConversation(conversation);
        noConversation.setVisibility(View.GONE);
    }

    @Override
    public void onChangeMessageState(String otherUserPhoneNumber, String messageUid, DataBase.MessageState messageState) {
        if (conversationAdapter != null)
            conversationAdapter.updateMessage(otherUserPhoneNumber);
    }

    @Override
    public void onDeleteMessage(String otherUserPhoneNumber, MessageModel messageModel) {
        if (conversationAdapter != null)
            conversationAdapter.updateMessage(otherUserPhoneNumber);
    }
}
