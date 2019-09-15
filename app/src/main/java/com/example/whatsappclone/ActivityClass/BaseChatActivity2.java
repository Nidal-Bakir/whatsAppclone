package com.example.whatsappclone.ActivityClass;

import android.Manifest;
import android.content.Intent;
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
import com.example.whatsappclone.Adapters.StatusAdapter;
import com.example.whatsappclone.AssistanceClass.InternetCheck;
import com.example.whatsappclone.AssistanceClass.OnSwipeListener;
import com.example.whatsappclone.AssistanceClass.BackgroundWorker;
import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.SyncContactsWithCloudDB;
import com.example.whatsappclone.WhatsAppFireStore.UploadMedia;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.example.whatsappclone.WhatsApp_Models.Status;
import com.example.whatsappclone.WhatsApp_Models.VisitStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import io.fabric.sdk.android.Fabric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BaseChatActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseChatActivity2";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
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
                View view =findViewById(R.id.chat_floating_bt);
                final Snackbar snackbar=Snackbar.make(view,"The app need all this permissions!!",Snackbar.LENGTH_INDEFINITE);
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
                                        statusCollectionListener();
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
        setContentView(R.layout.activity_base_chat2);
        //i.g US this will use with libphonenumber lib
        // to handle the numbers whose  doesn't have area code i.g(+1)
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        countryCode = tm.getSimCountryIso();
        // open the login activity
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
            finish();
        } else {
            // ask for all Permissions
            if (checkAndRequestPermissions()) {
                startApp();


            }
        }
    }

    @Override
    protected void onDestroy() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType
                .CONNECTED).build();
        OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(BackgroundWorker.class)
                .setConstraints(constraints).build();
        WorkManager.getInstance(getBaseContext()).enqueue(uploadWork);
        contactsWithCloudDB.cancel(true);

        super.onDestroy();
    }

    private void startApp() {
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("WhatsApp");
        //open the contacts activity
        FloatingActionButton fab = findViewById(R.id.chat_floating_bt);
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


            }
        });
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
                            //show the list of contacts hows visited the user storu
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

                    final String time = new SimpleDateFormat("h:mm a").format(new Date());
                    /*get the other user story and compare the uploading time if it's the same
                     * then send message that you are visit his/her story
                     * */
                    statusCollectionRef.document(status.getPhone_number())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Status otherUserStatus = task.getResult().toObject(Status.class);
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
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
        }
        dataBase = new DataBase(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataBase.close();
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

    @SuppressWarnings("StatementWithEmptyBody")
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
                startActivity(new Intent(BaseChatActivity2.this,SettingsActivity.class));

                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;


        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

}
