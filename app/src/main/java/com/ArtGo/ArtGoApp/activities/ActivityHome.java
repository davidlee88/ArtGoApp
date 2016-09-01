package com.ArtGo.ArtGoApp.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ArtGo.ArtGoApp.utils.RestClient;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.ArtGo.ArtGoApp.R;
import com.ArtGo.ArtGoApp.adapters.AdapterLocations;
import com.ArtGo.ArtGoApp.fragments.FragmentDialogError;
import com.ArtGo.ArtGoApp.listeners.OnTapListener;
import com.ArtGo.ArtGoApp.utils.DBHelperLocations;
import com.ArtGo.ArtGoApp.utils.Utils;
import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 *
 *
 * ActivityHome is created to display locations data in map view and list view.
 * Created using AppCompatActivity.
 */
public class ActivityHome extends AppCompatActivity
        implements ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback, SnackBar.OnMessageClickListener {

    private static final String TAG = ActivityHome.class.getSimpleName();

    // Create view objects
    private RecyclerView mList;
    private Spinner mSpnCategory;
    private FloatingActionButton mFabLayer, mFabLocation;
    private SupportMapFragment mMapFragment;
    private AdView mAdView;
    private Menu mMenu;
    private GoogleMap mMap;
    private MaterialDialog progressDialog;

    // Create adapter and dbhelper objects
    private AdapterLocations mAdapter;
    private DBHelperLocations mDBHelper;

    private boolean mIsMapVisible = true;
    private boolean mIsAppFirstLaunched = true;

    // Provides the entry point to Google Play services
    protected GoogleApiClient mGoogleApiClient;

    // The desired interval for location updates. Inexact. Updates may be more or less frequent
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // The fastest rate for active location updates. Exact. Updates will never be more frequent
    // than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Stores parameters for requests to the FusedLocationProviderApi
    protected LocationRequest mLocationRequest;

    // Constant used in the location settings dialog
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    // Stores the types of location services the client is interested in using. Used for checking
    // settings to determine if the device has optimal location settings.
    protected LocationSettingsRequest mLocationSettingsRequest;

    // Tracks the status of the location updates request. Value changes when the user presses the
    // Start Updates and Stop Updates buttons.
    protected Boolean mRequestingLocationUpdates = false;

    // Represents a geographical location
    protected Location mCurrentLocation;

    // Create arraylist variables to store data
    private ArrayList<String> mLocationIds          = new ArrayList<>();
    private ArrayList<String> mLocationNames        = new ArrayList<>();
    private ArrayList<String> mLocationImages       = new ArrayList<>();
    private ArrayList<String> mLocationAddresses    = new ArrayList<>();
    private ArrayList<Float> mLocationDistances     = new ArrayList<>();
    private ArrayList<String> mLocationLatitudes    = new ArrayList<>();
    private ArrayList<String> mLocationLongitudes   = new ArrayList<>();
    private ArrayList<String> mLocationMarkers      = new ArrayList<>();
    private ArrayList<String> mCategoryIds          = new ArrayList<>();
    private ArrayList<String> mCategoryNames        = new ArrayList<>();

    // To handle LocationDistance in String
    private ArrayList<String> mLocationDistancesString    = new ArrayList<>();

    // Create hashmap variable to store marker id and location id
    private HashMap<String, String> mLocationIdsOnMarkers = new HashMap<>();

    // Create string variable to store category id
    private String mSelectedCategoryId = "0";

    // Create objects and variables for map configuration
    float[] mCheckDistances = new float[1];
    private int mSelectedMapType;
    public static double mCurrentLatitude, mCurrentLongitude;
    private boolean mIsAdmobVisible;
    private int mLocationResultStatus = -1;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 0;

    // Flag permission
    private boolean mFlagGranted = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Connect view objects with view ids in xml
        CircleProgressBar 
             mPrgLoading = (CircleProgressBar) findViewById(R.id.prgLoading);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mList            = (RecyclerView) findViewById(R.id.list);
        mSpnCategory     = (Spinner) findViewById(R.id.spnCategory);
        mFabLayer        = (FloatingActionButton) findViewById(R.id.fabLayer);
        mFabLocation     = (FloatingActionButton) findViewById(R.id.fabLocation);
        mAdView          = (AdView) findViewById(R.id.adView);
        mMapFragment     = ((SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map));

        // Set default map type
        mSelectedMapType = Utils.ARG_DEFAULT_MAP_TYPE;

        // Set up map
        mMapFragment.getMapAsync(this);

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Configure recyclerview
        mList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mList.setLayoutManager(layoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());

        // Hide fab button first
        hideFab();

        // Set listener to fab buttons
        mFabLayer.setOnClickListener(this);
        mFabLocation.setOnClickListener(this);

        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);

        // Get admob visibility value
        mIsAdmobVisible = Utils.admobVisibility(mAdView, Utils.IS_ADMOB_VISIBLE);

        // Load ad in background using asynctask class
        //new SyncShowAd(mAdView).execute();

        // Check databases
        checkDatabase();

        // Set adapter object
        mAdapter = new AdapterLocations(this);

        // Get category data
        //new SyncGetCategories().execute();
        new SyncGetTypes().execute();

        // Build Google API client. Check if Google play services is available and get user position.
        buildGoogleApiClient();

        // Listener for spinner when item clicked
        mSpnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Save selected category id whenever category change
                mSelectedCategoryId = mCategoryIds.get(i);

                // If this is not the first time of the app run, load data in background
                // using asynctask class
                if(!mIsAppFirstLaunched) {
                    if (mCurrentLocation != null) {
                        new SyncGetLocations().execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Listener for recycler view when item clicked
        mAdapter.setOnTapListener(new OnTapListener() {
            @Override
            public void onTapView(int position) {
            // Open ActivityDetail when item in recyclerview clicked
            Intent detailIntent = new Intent(getApplicationContext(), ActivityDetail.class);
            detailIntent.putExtra(Utils.ARG_LOCATION_ID, mLocationIds.get(position));
            startActivity(detailIntent);
            overridePendingTransition(R.anim.open_next, R.anim.close_main);
            }
        });

        // Handle item menu in toolbar
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuView:
                        // Change data view to list view or map view
                        changeView();
                        return true;
                    case R.id.menuAbout:
                        // Open ActivityAbout when about item on toolbar clicked
                        Intent aboutIntent = new Intent(getApplicationContext(), ActivityAbout.class);
                        startActivity(aboutIntent);
                        overridePendingTransition(R.anim.open_next, R.anim.close_main);
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    // Method to hide fab buttons with scale animation
    public void hideFab(){
        ViewPropertyAnimator.animate(mFabLayer).cancel();
        ViewPropertyAnimator.animate(mFabLayer).scaleX(0).scaleY(0).setDuration(200).start();
        ViewPropertyAnimator.animate(mFabLocation).cancel();
        ViewPropertyAnimator.animate(mFabLocation).scaleX(0).scaleY(0).setDuration(200).start();
        mFabLayer.setVisibility(View.GONE);
        mFabLocation.setVisibility(View.GONE);
    }

    // Method to show fab buttons with scale animation
    public void showFab(){
        ViewPropertyAnimator.animate(mFabLayer).cancel();
        ViewPropertyAnimator.animate(mFabLayer).scaleX(1).scaleY(1).setDuration(200).start();
        ViewPropertyAnimator.animate(mFabLocation).cancel();
        ViewPropertyAnimator.animate(mFabLocation).scaleX(1).scaleY(1).setDuration(200).start();
        mFabLayer.setVisibility(View.VISIBLE);
        mFabLocation.setVisibility(View.VISIBLE);
    }

    // Method to change data view from list view to map view or vise versa
    public void changeView(){
        // If map is visible hide map and fab buttons. and display recycler view. Else hide recycler
        // view and display map and fab buttons.
        if (mIsMapVisible) {
            try {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.hide(mMapFragment);
                ft.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mList.setVisibility(View.VISIBLE);
            mIsMapVisible = false;
            mMenu.getItem(0).setIcon(R.mipmap.ic_map_white_36dp);
            hideFab();
        } else {
            try {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(mMapFragment);
                ft.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mList.setVisibility(View.GONE);
            mIsMapVisible = true;
            mMenu.getItem(0).setIcon(R.mipmap.ic_view_list_white_36dp);
            showFab();
        }
    }

    // Method to handle click on info window
    @Override
    public void onInfoWindowClick(Marker marker) {
        // Open ActivityDetail and pass location id
        String selectedLocationId = mLocationIdsOnMarkers.get(marker.getId());
        Intent detailIntent = new Intent(this, ActivityDetail.class);
        detailIntent.putExtra(Utils.ARG_LOCATION_ID, selectedLocationId);
        startActivity(detailIntent);
    }

    // Method to set up map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        progressDialog = new MaterialDialog.Builder(ActivityHome.this)
                .content(R.string.getting_user_position)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .show();
        
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        setMapType(mSelectedMapType);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        Log.d(Utils.TAG_PONGODEV + TAG, "onMapReady");
    }

    @Override
    public void onMessageClick(Parcelable token) {}

    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void>{

        AdView ad;
        AdRequest adRequest;

        public SyncShowAd(AdView ad){
            this.ad = ad;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Check ad visibility. If visible, create adRequest
            if(mIsAdmobVisible) {
                // Create an ad request
                if (Utils.IS_ADMOB_IN_DEBUG) {
                    adRequest = new AdRequest.Builder().
                            addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
                } else {
                    adRequest = new AdRequest.Builder().build();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Check ad visibility. If visible, display ad banner and interstitial
            if(mIsAdmobVisible) {
                // Start loading the ad
                ad.loadAd(adRequest);

            }

        }
    }

    //Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null && checkGooglePlayService()) {
            Log.d(Utils.TAG_PONGODEV + TAG, "Building GoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            /**
             * Sets up the location request. Android has two location request settings:
             * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings
             * control the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION,
             * as defined in the AndroidManifest.xml.
             * <p/>
             * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
             * interval (5 seconds), the Fused Location Provider API returns location updates
             * that are accurate to within a few feet.
             * <p/>
             * These settings are appropriate for mapping applications that show real-time location
             * updates.
             */
            mLocationRequest = new LocationRequest();

            // Sets the desired interval for active location updates. This interval is
            // inexact. You may not receive updates at all if no location sources are available, or
            // you may receive them slower than requested. You may also receive updates faster than
            // requested if other applications are requesting location at a faster interval.
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates faster than this value.
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            /**
             * Uses a {@link LocationSettingsRequest.Builder} to build
             * a {@link LocationSettingsRequest} that is used for checking
             * if a device has the needed location settings.
             */
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);
            mLocationSettingsRequest = builder.build();

            /**
             * Check if the device's location settings are adequate for the app's needs using the
             * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings
             * (GoogleApiClient,LocationSettingsRequest)} method,
             *  with the results provided through a {@code PendingResult}.
             */
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(
                            mGoogleApiClient,
                            mLocationSettingsRequest
                    );
            result.setResultCallback(this);

        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(0, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
        }

    }

    //**** Start: Setting Location ****//
    // Check google play service if available run map
    private boolean checkGooglePlayService() {
        /**
         * verify that Google Play services is available before making a request.
         *
         * @return true if Google Play services is available, otherwise false
         */
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(Utils.TAG_PONGODEV + TAG, getString(R.string.play_services_available));
            return true;

            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
            return false;
        }
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d(Utils.TAG_PONGODEV + TAG, "All location settings are satisfied.");
                if (mCurrentLocation == null) {
                    Log.d(Utils.TAG_PONGODEV + TAG, "onResult SUCCESS mCurrentLocation == null");
                    mCurrentLocation = LocationServices.FusedLocationApi
                            .getLastLocation(mGoogleApiClient);

                }
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.d(Utils.TAG_PONGODEV + TAG,
                        "Location settings are not satisfied. Show the user a dialog to" +
                                "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(ActivityHome.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.d(Utils.TAG_PONGODEV + TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d(Utils.TAG_PONGODEV + TAG,
                        "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mLocationResultStatus = resultCode;
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            // If OK selected, then update user location, else if no button selected use default
            // location.
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // If GPS enabled, start location update to get user position
                        Log.d(Utils.TAG_PONGODEV + TAG,
                                "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // If GPS not enabled, set default user position
                        mCurrentLocation = new Location("");
                        mCurrentLocation.setLatitude(Utils.ARG_DEFAULT_LATITUDE);
                        mCurrentLocation.setLongitude(Utils.ARG_DEFAULT_LONGITUDE);
                        mCurrentLatitude = Utils.ARG_DEFAULT_LATITUDE;
                        mCurrentLongitude = Utils.ARG_DEFAULT_LONGITUDE;

                        getUserPosition(mCurrentLatitude, mCurrentLongitude);
                        new SyncGetLocations().execute();
                        showFab();
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(Utils.TAG_PONGODEV + TAG, "Connection suspended");
    }

    /*
     * called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(Utils.TAG_PONGODEV + TAG, "onConnectionFailed");
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        9000);

                /*
                * thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
                Log.i("onConnectionFailed", ""+e);
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(0, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(errorFragment, null);
                ft.commitAllowingStateLoss();
            }
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // When request permission mFlagGranted false
        mFlagGranted = false;
        if (!mayRequestLocations()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 8) {
            // It can prosses after get permission(Marshmallow)
            mMap.setMyLocationEnabled(true);
            // after get permission mFlagGranted true
            mFlagGranted = true;

            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    mRequestingLocationUpdates = true;
                }
            });
            Log.d(Utils.TAG_PONGODEV + TAG, "startLocationUpdates");
        }
    }

    // Implement permissions requests on apps that target API level 23 or higher, and are
    // running on a device that's running Android 6.0 (API level 23) or higher.
    // If the device or the app's targetSdkVersion is 22 or lower, the system prompts the user
    // to grant all dangerous permissions when they install or update the app.
    private boolean mayRequestLocations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // If already choose deny once
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            askPermissionDialog();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
        return false;

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // location-related task you need to do.
                startLocationUpdates();
                Log.d(Utils.TAG_PONGODEV + TAG, "Request Location Allowed");
            } else {
                // permission was not granted
                if (getApplicationContext() == null) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        startLocationUpdates();
                    } else {
                        permissionSettingDialog();
                    }
                }
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
            }
        });
    }

    // Method to set map type based on dialog map type
    private void setMapType(int position) {
        switch (position) {
            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 3:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }
    //**** End: Setting Location ****//

    // Method to check database
    private void checkDatabase() {
        // Create object of database helpers
        mDBHelper = new DBHelperLocations(this);

        // Create database
        try {
            mDBHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        // Open recipes database
        mDBHelper.openDataBase();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabLayer:
                // Display map type dialog
                showMapTypeDialog();
                break;
            case R.id.fabLocation:
                // Get user position
                mCurrentLatitude = mCurrentLocation.getLatitude();
                mCurrentLongitude = mCurrentLocation.getLongitude();
                getUserPosition(mCurrentLatitude, mCurrentLongitude);
                break;
        }
    }

    // Method to get user position
    public void getUserPosition(double latitude, double longitude){
        // Check distance between user position and default position
        Location.distanceBetween(latitude, longitude,
                Utils.ARG_DEFAULT_LATITUDE, Utils.ARG_DEFAULT_LONGITUDE, mCheckDistances);

        if ((mCheckDistances[0] / 1000) > Utils.ARG_MAX_DISTANCE) {
            mCurrentLocation = new Location("");
            mCurrentLocation.setLatitude(Utils.ARG_DEFAULT_LATITUDE);
            mCurrentLocation.setLongitude(Utils.ARG_DEFAULT_LONGITUDE);
            mCurrentLatitude = mCurrentLocation.getLatitude();
            mCurrentLongitude = mCurrentLocation.getLongitude();

            if(mLocationResultStatus == Activity.RESULT_CANCELED){
                showSnackbar(getString(R.string.gps_not_enabled_alert));
            }else {
                showSnackbar(getString(R.string.distance_alert));
            }
        }else{
            mCurrentLatitude = latitude;
            mCurrentLongitude = longitude;
        }

        // Move camera to user position
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentLatitude, mCurrentLongitude),
                Utils.ARG_DEFAULT_MAP_ZOOM_LEVEL);
        mMap.animateCamera(cameraUpdate);

    }

    // Method to show snackbar message
    public void showSnackbar(String message){
        // Inform user that they will use default position using snackbar
        new SnackBar.Builder(ActivityHome.this)
            .withMessage(message)
            .withVisibilityChangeListener(new SnackBar.OnVisibilityChangeListener() {
                @Override
                public void onShow(int i) {
                    // While snackbar is visible, change fab buttons position above
                    ViewPropertyAnimator.animate(mFabLayer).cancel();
                    ViewPropertyAnimator.animate(mFabLayer).translationY(-80)
                            .setDuration(200).start();
                    ViewPropertyAnimator.animate(mFabLocation).cancel();
                    ViewPropertyAnimator.animate(mFabLocation).translationY(-80)
                            .setDuration(200).start();
                }

                @Override
                public void onHide(int i) {
                    // While snackbar is not visible, set fab buttons position to default
                    ViewPropertyAnimator.animate(mFabLayer).cancel();
                    ViewPropertyAnimator.animate(mFabLayer).translationY(0)
                            .setDuration(200).start();
                    ViewPropertyAnimator.animate(mFabLocation).cancel();
                    ViewPropertyAnimator.animate(mFabLocation).translationY(0)
                            .setDuration(200).start();
                }
            })
            .show();
    }

    // Method to display map type dialog
    public void showMapTypeDialog() {
        new MaterialDialog.Builder(this)
            .title(R.string.dialog_map_type_title)
            .items(R.array.map_types)
            .itemsCallbackSingleChoice(mSelectedMapType,
                    new MaterialDialog.ListCallbackSingleChoice() {
                @Override
                public boolean onSelection(MaterialDialog dialog,
                                           View view, int selectedIndex, CharSequence text) {
                    mSelectedMapType = selectedIndex;
                    setMapType(mSelectedMapType);
                    return true;
                }
            }).positiveText(R.string.select)
            .show();
    }
    //sync method to get types of locations
    public class SyncGetTypes extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Clear arraylist variable first before used
            mCategoryIds.clear();
            mCategoryNames.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get category data from RestClient
            getCategoryFromRestServer();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Set category data to spinner
            ArrayAdapter<String> categoryAdapter;
            categoryAdapter = new ArrayAdapter<>(
                    getApplicationContext(),
                    R.layout.layout_spinner,
                    mCategoryNames
            );
            categoryAdapter.setDropDownViewResource(R.layout.layout_spinner);
            mSpnCategory.setAdapter(categoryAdapter);
        }
    }
    // Asynctask class to get category data from database
   /* public class SyncGetCategories extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Clear arraylist variable first before used
            mCategoryIds.clear();
            mCategoryNames.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get category data from database
            getCategoryFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Set category data to spinner
            ArrayAdapter<String> categoryAdapter;
            categoryAdapter = new ArrayAdapter<>(
                    getApplicationContext(),
                    R.layout.layout_spinner,
                    mCategoryNames
            );
            categoryAdapter.setDropDownViewResource(R.layout.layout_spinner);
            mSpnCategory.setAdapter(categoryAdapter);
        }
    }*/

    // Method to get data from database
    /*public void getCategoryFromDatabase(){
        ArrayList<ArrayList<Object>> dataCategory = mDBHelper.getAllCategoriesData();

        // Ad "All Places" in first row
        mCategoryIds.add("0");
        mCategoryNames.add(getString(R.string.all_places));
        for(int i = 0; i< dataCategory.size(); i++){
            ArrayList<Object> row = dataCategory.get(i);

            mCategoryIds.add(row.get(0).toString());
            mCategoryNames.add(row.get(1).toString());
        }
    }*/

    //Method to get data from Rest Server
    public void getCategoryFromRestServer(){
        JSONArray locationType = RestClient.getAllCategoriesData();

        // Ad "All Places" in first row
        mCategoryIds.add("0");
        mCategoryNames.add(getString(R.string.all_places));
        for(int i = 0; i< locationType.length(); i++){
            try{
                JSONObject row = locationType.getJSONObject(i);
                mCategoryIds.add(row.get("id").toString());
                mCategoryNames.add(row.get("name").toString());
        }catch (Exception e){

            }
        }
    }

    // Asynctask class to load location data from database
    public class SyncGetLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // If this is not the first time app launched, set up and display progress dialog
            if(!mIsAppFirstLaunched) {
                progressDialog = new MaterialDialog.Builder(ActivityHome.this)
                        .content(R.string.loading_data)
                        .progress(true, 0)
                        .progressIndeterminateStyle(false)
                        .cancelable(false)
                        .show();
            }else{
                progressDialog.setContent(R.string.loading_data);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get data from database
            getLocationDataFromRestClient();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Set data to adapter
            mAdapter.updateList(mLocationIds, mLocationNames,
                    mLocationAddresses, mLocationDistancesString, mLocationImages);

            // Set adapter to recycler view
            mList.setAdapter(mAdapter);

            // if this is the first time app launched, than set it to false
            if(mIsAppFirstLaunched){
                mIsAppFirstLaunched = false;
            }
            progressDialog.dismiss();

            // Set up map after getting location data from database
            setupMarker();
        }
    }

    // Method to retrieve locations data from database
    /*private void getLocationDataFromDatabase(){
        // Clear arraylist variables first before storing data
        mLocationIds.clear();
        mLocationNames.clear();
        mLocationImages.clear();
        mLocationAddresses.clear();
        mLocationLatitudes.clear();
        mLocationLongitudes.clear();
        mLocationDistances.clear();
        mLocationMarkers.clear();
        mLocationDistancesString.clear();

        // Store data to arraylist variables
        ArrayList<ArrayList<Object>> dataLocation =
                mDBHelper.getLocationsByCategory(mSelectedCategoryId);
        float[] distances = new float[1];

        for(int i = 0; i< dataLocation.size(); i++){
            ArrayList<Object> row = dataLocation.get(i);
            mLocationIds.add(row.get(0).toString());
            mLocationNames.add(row.get(1).toString());
            mLocationAddresses.add(row.get(2).toString());
            mLocationImages.add(row.get(3).toString());
            mLocationLatitudes.add(row.get(4).toString());
            mLocationLongitudes.add(row.get(5).toString());
            mLocationMarkers.add(row.get(6).toString());
            // Check distance between locations and user position first
            Location.distanceBetween(Double.valueOf(row.get(4).toString()),
                    Double.valueOf(row.get(5).toString()),
                    mCurrentLatitude, mCurrentLongitude, distances);

            float paramDistance = (distances[0] / 1000);
            mLocationDistances.add(paramDistance);

            // For trigger variable mLocationDistancesString
            mLocationDistancesString.add(String.format("%.2f", paramDistance));

        }

        // Sort data base on distance between location and user position
        sortDataByDistance();

    }*/

    private void getLocationDataFromRestClient(){
        // Clear arraylist variables first before storing data
        mLocationIds.clear();
        mLocationNames.clear();
        mLocationImages.clear();
        mLocationAddresses.clear();
        mLocationLatitudes.clear();
        mLocationLongitudes.clear();
        mLocationDistances.clear();
        mLocationMarkers.clear();
        mLocationDistancesString.clear();

        // Store data to arraylist variables
       JSONArray locations = RestClient.getLocationsByCategory(mSelectedCategoryId);
        float[] distances = new float[1];

        for(int i = 0; i< locations.length(); i++) {
            try {
                JSONObject row = locations.getJSONObject(i);
                mLocationIds.add(row.get("id").toString());
                mLocationNames.add(row.get("name").toString());
                mLocationAddresses.add(row.get("address").toString());
                mLocationImages.add(row.get("image").toString());
                String[] parts = row.get("geolocation").toString().split(",");
                String part1 = parts[0].replace("(", "");
                String part2 = parts[1].replace(")", "");
                mLocationLatitudes.add(part1);
                mLocationLongitudes.add(part2);
                mLocationMarkers.add(row.get("marker").toString());
                // Check distance between locations and user position first
                Location.distanceBetween(Double.valueOf(part1),
                        Double.valueOf(part2),
                        mCurrentLatitude, mCurrentLongitude, distances);

                float paramDistance = (distances[0] / 1000);
                mLocationDistances.add(paramDistance);

                // For trigger variable mLocationDistancesString
                mLocationDistancesString.add(String.format("%.2f", paramDistance));

            } catch (Exception e) {

            }
        }
            // Sort data base on distance between location and user position
            sortDataByDistance();



    }

    // Method to set up location markers
    private void setupMarker(){
        // Clear map before displaying marker
        mMap.clear();

        // Add marker to all locations
        for(int i = 0; i< mLocationLatitudes.size(); i++){

            int marker = getResources().getIdentifier(
                    mLocationMarkers.get(i), "mipmap", getPackageName());

            Marker locationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(mLocationLatitudes.get(i)),
                            Double.valueOf(mLocationLongitudes.get(i))))
                    .icon(BitmapDescriptorFactory.fromResource(marker))
                    .snippet(mLocationAddresses.get(i))
                    .title(mLocationNames.get(i)));

            mLocationIdsOnMarkers.put(locationMarker.getId(), mLocationIds.get(i));

        }

    }

    // Method to sort data by distance
    private void sortDataByDistance()    {
        int j;
        boolean flag = true;   // set flag to true to begin first pass
        Float tempDistance;   //holding variable
        String tempId, tempName, tempLatitude, tempLongitude, tempAddress, tempImage, tempMarker;
        while ( flag )
        {
            flag= false;    //set flag to false awaiting a possible swap
            for( j=0;  j < mLocationDistances.size()-1;  j++ )
            {
                // change to > for ascending sort, < for descending
                if ( mLocationDistances.get(j)> mLocationDistances.get(j+1) )
                {
                    //swap elements
                    tempDistance    = mLocationDistances.get(j);
                    tempId          = mLocationIds.get(j);
                    tempName        = mLocationNames.get(j);
                    tempLatitude    = mLocationLatitudes.get(j);
                    tempLongitude   = mLocationLongitudes.get(j);
                    tempAddress     = mLocationAddresses.get(j);
                    tempImage       = mLocationImages.get(j);
                    tempMarker      = mLocationMarkers.get(j);

                    mLocationDistances.set(j, mLocationDistances.get(j + 1));

                    // Setting mLocationDistancesString in string format with 2 decimal
                    mLocationDistancesString.set(j, String.format("%.2f",
                            mLocationDistances.get(j + 1)));

                    mLocationIds.set(j, mLocationIds.get(j + 1));
                    mLocationNames.set(j, mLocationNames.get(j + 1));
                    mLocationLatitudes.set(j,mLocationLatitudes.get(j+1) );
                    mLocationLongitudes.set(j,mLocationLongitudes.get(j+1) );
                    mLocationAddresses.set(j,mLocationAddresses.get(j+1) );
                    mLocationImages.set(j,mLocationImages.get(j+1) );
                    mLocationMarkers.set(j,mLocationMarkers.get(j+1) );

                    mLocationDistances.set(j + 1, tempDistance);
                    mLocationDistancesString.set(j + 1, String.format("%.2f", tempDistance));

                    mLocationIds.set(j+1,tempId);
                    mLocationNames.set(j+1,tempName);
                    mLocationLatitudes.set(j+1,tempLatitude);
                    mLocationLongitudes.set(j+1,tempLongitude);
                    mLocationAddresses.set(j+1,tempAddress);
                    mLocationImages.set(j+1,tempImage);
                    mLocationMarkers.set(j+1,tempMarker);

                    flag = true;              //shows a swap occurred
                } else {
                    // Setting mLocationDistancesString in string format with 2 decimal
                    mLocationDistancesString.set(j, String.format("%.2f", mLocationDistances.get(j)));
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if(mIsAppFirstLaunched) {

            getUserPosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            new SyncGetLocations().execute();
            showFab();

            // Condition after get current location it not search again
            stopLocationUpdates();
        }
    }

    // Method to display share dialog
    private void askPermissionDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .backgroundColorRes(R.color.material_background_color)
            .titleColorRes(R.color.primary_text)
            .contentColorRes(R.color.primary_text)
            .positiveColorRes(R.color.accent_color)
            .negativeColorRes(R.color.accent_color)
            .title(R.string.permission)
            .content(R.string.request_location)
            .positiveText(R.string.open_permission)
            .negativeText(R.string.close_app)
            .cancelable(false)
            .callback(new MaterialDialog.ButtonCallback() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onPositive(MaterialDialog dialog) {
                    requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    // Close dialog when Cancel button clicked
                    finish();
                }
            }).build();
        // Show dialog
        dialog.show();
    }

    // Method to display setting dialog
    private void permissionSettingDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .backgroundColorRes(R.color.material_background_color)
                .titleColorRes(R.color.primary_text)
                .contentColorRes(R.color.primary_text)
                .negativeColorRes(R.color.accent_color)
                .title(R.string.permission)
                .content(R.string.request_location_permission)
                .negativeText(R.string.close_app)
            .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        // Close dialog when Cancel button clicked
                        finish();
                }
            }).build();
        // Show dialog
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(Utils.TAG_PONGODEV + TAG, "onStart");
        if(checkGooglePlayService()) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Utils.TAG_PONGODEV + TAG, "onPause");
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if(checkGooglePlayService()) {
            // Stop location updates to save battery,
            // but don't disconnect the GoogleApiClient object.
            if (mGoogleApiClient.isConnected()) {
                stopLocationUpdates();
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(Utils.TAG_PONGODEV + TAG, "onResume");

        if (mAdView != null) {
            mAdView.resume();
        }
        if(checkGooglePlayService()) {
            if(mGoogleApiClient.isConnected() && mFlagGranted){
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Utils.TAG_PONGODEV + TAG, "onStop");
            if(checkGooglePlayService()) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Utils.TAG_PONGODEV + TAG, "onDestroy");

        if (mAdView != null) {
            mAdView.destroy();
        }
        mDBHelper.close();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Utils.TAG_PONGODEV + TAG, "Wait until user position");
    }

}

