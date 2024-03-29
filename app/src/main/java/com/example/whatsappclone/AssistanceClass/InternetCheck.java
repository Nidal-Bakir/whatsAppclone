package com.example.whatsappclone.AssistanceClass;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;

public class InternetCheck extends AsyncTask<Void,Void,Boolean> {
    private static final String TAG = "InternetCheck";
    private OnCheckComplete onCheckComplete;
    private Context context;

    public InternetCheck(Context context) {
        this.context = context;
    }

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 172.217.12.174");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            Log.d(TAG, "isOnline: ", e);
        } catch (InterruptedException e) {
            Log.d(TAG, "isOnline: ", e);
        }

        return false;
    }

    //check if the network Available
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    //check connection to the FireBase server
    private static Boolean checkInternetConnection(Context context) {
        return isNetworkAvailable(context) && InternetCheck.isOnline();
    }


    @Override
    public Boolean doInBackground(Void... voids) {
        return checkInternetConnection(context);
    }

    @Override
    public void onPostExecute(Boolean aBoolean) {
       onCheckComplete.onCheckComplete(aBoolean);
    }
    public void onComplete(OnCheckComplete onCheckcomplete){
        this.onCheckComplete=onCheckcomplete;
    }
    public interface OnCheckComplete {
        void onCheckComplete(boolean isOnline);

    }
}


