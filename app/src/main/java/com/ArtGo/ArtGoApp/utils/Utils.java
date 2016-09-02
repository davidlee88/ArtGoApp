package com.ArtGo.ArtGoApp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.google.android.gms.ads.AdView;

/**
 *
 *
 * Utils is created to set application configuration, from database path, ad visibility.
 */
public class Utils {

    // Debugging tag
    public static final String TAG_PONGODEV             = "ArtGo:";
    public static final String TAG_ACTIVITY_DIRECTIONS  = "ActivityDirection";

    // Key values for passing data between activities
    public static final String ARG_LOCATION_ID         = "location_id";
    public static final String ARG_LOCATION_NAME       = "location_name";
    public static final String ARG_LOCATION_ADDRESSES  = "location_addresses";
    public static final String ARG_LOCATION_LONGITUDE  = "location_longitude";
    public static final String ARG_LOCATION_LATITUDE   = "location_latitude";
    public static final String ARG_LOCATION_MARKER     = "location_marker";

    // Key values for ad interstitial trigger
    public static final String ARG_TRIGGER = "trigger";

    // Configurable parameters. you can configure these parameter.
    // Set database path. It must be similar with package name.
    public static final String ARG_DATABASE_PATH = "/data/data/com.ArtGo.ArtGoApp/databases/";

    // Set default max distance between current user position and default user position
    public static final float ARG_MAX_DISTANCE = (float) 100.0; // In kilometers
    // Set default map zoom level. Set value from 1 to 17
    public static final int ARG_DEFAULT_MAP_ZOOM_LEVEL = 11;

    // Set default map type. 0 is normal, 1, is hybrid, 2, is satellite, and 3 is terrain.
    public static final int ARG_DEFAULT_MAP_TYPE = 0;

    // Set default user position if user decide to not enable location
    public static final Double ARG_DEFAULT_LATITUDE  = -37.8857384;
    public static final Double ARG_DEFAULT_LONGITUDE = 145.0548504;

    // For every ActivityDetail display you want to interstitial ad show up.
    // 3 means interstitial ad will show up after user open ActivityDetail page three times.
    public static final int ARG_TRIGGER_VALUE = 3;

    // Admob visibility parameter. Set true to show admob and false to hide.
    public static final boolean IS_ADMOB_VISIBLE = false;

    // Set value to true if you are still in development process, and false if you are ready to publish the app.
    public static final boolean IS_ADMOB_IN_DEBUG = false;

    // Default is 2500
    public static final Integer ARG_TIMEOUT_MS  = 4000;

    // Method to check admob visibility
    public static boolean admobVisibility(AdView ad, boolean isInDebugMode){
        if(isInDebugMode) {
            ad.setVisibility(View.VISIBLE);
            return true;
        }else {
            ad.setVisibility(View.GONE);
            return false;
        }
    }

    // Method to load data that stored in SharedPreferences
    public static int loadPreferences(Context ctx, String param){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("user_data", 0);
        return sharedPreferences.getInt(param, 0);
    }

    // Method to save data to SharedPreferences
    public static void savePreferences(Context ctx, String param, int value){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(param, value);
        editor.apply();
    }

}