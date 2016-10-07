package com.ArtGo.ArtGoApp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ArtGo.ArtGoApp.R;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 *
 * ActivitySplash is created to display welcome screen.
 * Created using AppCompatActivity.
 */
public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Configuration in Android API below 21 to set window to full screen.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        // Create loading to wait for few second before displaying ActivityHome
        new Loading().execute();
    }

    // Asynctask class to process loading in background
    public class Loading extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Thread.sleep(2000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //Thread to make introduction screen only run once when user first launch the app.
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    //  Initialize SharedPreferences
                    SharedPreferences getPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getBaseContext());

                    //  Create a new boolean and preference and set it to true
                    boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                    //  If the activity has never started before...
                    if (isFirstStart) {

                        //  Launch app intro
                        Intent i = new Intent(ActivitySplash.this, ActivityIntro.class);
                        startActivity(i);
                        //  Make a new preferences editor
                        SharedPreferences.Editor e = getPrefs.edit();

                        //  Edit preference to make it false because we don't want this to run again
                        e.putBoolean("firstStart", false);

                        //  Apply changes
                        e.apply();
                    }else{
                        // When progress finished, open ActivityHome
                        Intent homeIntent = new Intent(getApplicationContext(), ActivityHome.class);
                        //Intent homeIntent = new Intent(ActivitySplash.this, ActivityIntro.class);
                        startActivity(homeIntent);
                    }
                }
            });

            // Start the thread
            t.start();

            overridePendingTransition(R.anim.open_next, R.anim.close_main);
        }
    }

    // Configuration in Android API 21 to set window to full screen.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }


}
