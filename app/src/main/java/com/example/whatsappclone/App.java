package com.example.whatsappclone;

import android.app.Application;

import com.droidnet.DroidNet;

public class App extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
        DroidNet.init(this);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();

    }
}
