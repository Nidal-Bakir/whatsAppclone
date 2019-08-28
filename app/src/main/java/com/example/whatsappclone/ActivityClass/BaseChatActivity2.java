package com.example.whatsappclone.ActivityClass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.whatsappclone.R;
import com.example.whatsappclone.WhatsAppDataBase.DataBase;
import com.example.whatsappclone.WhatsAppFireStore.SyncContactsWithCloudDB;
import com.example.whatsappclone.WhatsAppFireStore.UserSettings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.telephony.TelephonyManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import de.hdodenhof.circleimageview.CircleImageView;

public class BaseChatActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "BaseChatActivity2";
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final int RECORD_AUDIO_REQUEST_CODE = 288;
    private static final int CAMERA_REQUEST_CODE = 955;
    private static final int READ_CONTACTS_REQUEST_CODE = 873;
    private static final int WRITE_CONTACTS_REQUEST_CODE = 29;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 13;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 481;
    private static final int SEND_SMS_REQUEST_CODE = 124;
    private TextView profilePhoneNumber;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to RECORD voice messages", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to take photo ", Toast.LENGTH_SHORT).show();
                }
                break;
            case READ_CONTACTS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to read your contacts", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_CONTACTS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied on write ", Toast.LENGTH_SHORT).show();
                }
                break;
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to write on your External storage", Toast.LENGTH_SHORT).show();
                }
                break;
            case SEND_SMS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to send SMS", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_chat2);
        //i.g US this will use with libphonenumber lib
        // to handle the numbers whose  doesn't have area code i.g(+1)
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        final String countryCode = tm.getSimCountryIso();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
            finish();
        } else {
            // ask for all Permissions
            AskForPermissions();
            // for DataBase Debug
            Stetho.initializeWithDefaults(this);
            //connect to DataBase
            DataBase dataBase = new DataBase(this);
            //sync the contacts
            SyncContactsWithCloudDB contactsWithClouldDB = new SyncContactsWithCloudDB(getApplicationContext(), countryCode);
            contactsWithClouldDB.execute(false);

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
                PhoneNumberUtil phoneNumberUtil=PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber myNumber=phoneNumberUtil.parse(UserSettings.PHONENUMBER,countryCode.toUpperCase());
                profilePhoneNumber.setText(phoneNumberUtil.format(myNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
            } catch (NumberParseException e) {
                e.printStackTrace();
                profilePhoneNumber.setText(UserSettings.PHONENUMBER);
            }
            CircleImageView profileImage=nav.findViewById(R.id.profile_Image);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO : select image from studio or gallery
                    Toast.makeText(BaseChatActivity2.this, "select image ", Toast.LENGTH_SHORT).show();
                }
            });



        }
    }


    private void AskForPermissions() {
        if (Build.VERSION.SDK_INT > 23) {
            //  RECORD AUDIO PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                    Toast.makeText(this, " the app need this permission to send voice messages!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
            // READ CONTACTS PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS))
                    Toast.makeText(this, " the app need this permission to Sync you contacts with our servers!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
            // WRITE CONTACTS PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS))
                    Toast.makeText(this, " the app need this permission to Sync you contacts with our servers!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, WRITE_CONTACTS_REQUEST_CODE);
            // WRITE EXTERNAL STORAGE PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    Toast.makeText(this, " the app need this permission to store your data!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            //READ EXTERNAL STORAGE PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
                    Toast.makeText(this, " the app need this permission to store and read your data!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
            // CAMERA PERMISSION
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
                    Toast.makeText(this, " the app need this permission to send images!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            if (ContextCompat.checkSelfPermission(this
                    , Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS))
                    Toast.makeText(this, " the app need this permission to send SMS!", Toast.LENGTH_SHORT).show();
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);


        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(BaseChatActivity2.this, MainActivity.class));
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_newGroup) {
            //ToDO:handel the new group action
        } else if (id == R.id.nav_contacts) {
            startActivity(new Intent(BaseChatActivity2.this, ContactsActivity.class));
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
