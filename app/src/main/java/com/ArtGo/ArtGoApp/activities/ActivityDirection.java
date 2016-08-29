package com.ArtGo.ArtGoApp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.ArtGo.ArtGoApp.R;
import com.ArtGo.ArtGoApp.fragments.FragmentDialogError;
import com.ArtGo.ArtGoApp.libs.GoogleDirection;
import com.ArtGo.ArtGoApp.utils.Utils;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Document;

/**
 * Design and developed by pongodev.com
 *
 * ActivityDirection is created to display direction to location from user position.
 * Created using AppCompatActivity.
 */
public class ActivityDirection extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        OnMapReadyCallback,
        View.OnClickListener{

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = false;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    // Create variable to store user and location position value
    private LatLng mStart;
    private LatLng mEnd;
    private String mLocationName, mLocationAddress, mLocationMarker;
    private boolean mIsDirectionEnabled = true;

    private MaterialDialog mProgressDialog;
    private FloatingActionButton mFabDirection;

    // Create Google Direction objects
    private GoogleMap mMap;
    private GoogleDirection mGoogleDirection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        // Get data that passed from previous activity and store them in the variables
        Intent i            = getIntent();
        mLocationName       = i.getStringExtra(Utils.ARG_LOCATION_NAME);
        mLocationAddress    = i.getStringExtra(Utils.ARG_LOCATION_ADDRESSES);
        mLocationMarker     = i.getStringExtra(Utils.ARG_LOCATION_MARKER);
        double mLocationLatitude = Double.parseDouble(i.getStringExtra(Utils.ARG_LOCATION_LATITUDE));
        double mLocationLongitude = Double.parseDouble(i.getStringExtra(Utils.ARG_LOCATION_LONGITUDE));

        // Create start position (user position)
        mEnd = new LatLng(mLocationLatitude, mLocationLongitude);

        // Connect view objects with view ids in xml
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFabDirection    = (FloatingActionButton) findViewById(R.id.fabDirection);
        SupportMapFragment mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));

        mFabDirection.setOnClickListener(this);

        mMapFragment.getMapAsync(this);

        // Set toolbar as actionbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show progress dialog in the beginning
        mProgressDialog = new MaterialDialog.Builder(this)
            .content(R.string.getting_user_position)
            .progress(true, 0)
            .progressIndeterminateStyle(false)
            .cancelable(false)
            .show();

        // Check user position
        buildGoogleApiClient();

        // Draw direction route between user position and place position
        mGoogleDirection = new GoogleDirection(this);
        mGoogleDirection.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                mMap.addPolyline(gd.getPolyline(doc, 5, getResources().getColor(R.color.dark_primary_color)));

                Log.d(Utils.TAG_ACTIVITY_DIRECTIONS, status);
                if (status.equals(mGoogleDirection.STATUS_ZERO_RESULTS)){
                    showSnackbar(getString(R.string.unable_to_get_direction));
                }
                mProgressDialog.dismiss();

            }
        });
    }

    // Method to show snackbar message
    public void showSnackbar(String message){
        // Inform user that they will use default position using snackbar
        new SnackBar.Builder(ActivityDirection.this)
            .withMessage(message)
            .withVisibilityChangeListener(new SnackBar.OnVisibilityChangeListener() {
                @Override
                public void onShow(int i) {
                    // While snackbar is visible, change fab buttons position above
                    ViewPropertyAnimator.animate(mFabDirection).cancel();
                    ViewPropertyAnimator.animate(mFabDirection).translationY(-80)
                            .setDuration(200).start();
                }

                @Override
                public void onHide(int i) {
                    // While snackbar is not visible, set fab buttons position to default
                    ViewPropertyAnimator.animate(mFabDirection).cancel();
                    ViewPropertyAnimator.animate(mFabDirection).translationY(0)
                            .setDuration(200).start();
                }
            })
            .show();
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
            Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, getString(R.string.play_services_available));
            return true;

            // Google Play services was not available for some reason
        } else {
            // Dismiss progress dialog and display an error dialog
            mProgressDialog.dismiss();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Utils.TAG_ACTIVITY_DIRECTIONS);

            }
            return false;
        }
    }

    // Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null && checkGooglePlayService()) {
            Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "Building GoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

            /**
             * Sets up the location request. Android has two location request settings:
             * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
             * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
             * the AndroidManifest.xml.
             * <p/>
             * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
             * interval (5 seconds), the Fused Location Provider API returns location updates that are
             * accurate to within a few feet.
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
             * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
             * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
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
                mProgressDialog.dismiss();
                FragmentDialogError errorFragment = new FragmentDialogError();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), Utils.TAG_ACTIVITY_DIRECTIONS);
            }
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
                Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "All location settings are satisfied.");
                mProgressDialog.show();
                if (mCurrentLocation == null) {
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                }
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(Utils.TAG_ACTIVITY_DIRECTIONS,
                        "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(ActivityDirection.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(Utils.TAG_ACTIVITY_DIRECTIONS,
                        "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS,
                                "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        mProgressDialog.show();
                        break;
                    case Activity.RESULT_CANCELED:
                        mProgressDialog.dismiss();
                        finish();
                        break;
                }
                break;
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
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

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "Connection suspended");
    }

    /*
     * called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "onConnectionFailed");
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
                errorFragment.show(getSupportFragmentManager(), Utils.TAG_ACTIVITY_DIRECTIONS+
                        connectionResult.getErrorCode());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Get user position
        mCurrentLocation = location;
        mStart = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        // If this is the first time, create direction path and location marker
        if(mIsDirectionEnabled) {
            mIsDirectionEnabled = false;
            getDirection(mStart);
        }
    }

    // Method to get direction, create direction path, and location marker
    public void getDirection(LatLng start) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, Utils.ARG_DEFAULT_MAP_ZOOM_LEVEL));

        // Set location marker on map
        int marker = getResources().getIdentifier(mLocationMarker, "mipmap", getPackageName());
        mMap.addMarker(new MarkerOptions()
            .position(mEnd)
            .icon(BitmapDescriptorFactory.fromResource(marker))
            .snippet(mLocationAddress)
            .title(mLocationName));

        // Request direction from user position to location position
        mGoogleDirection.request(start, mEnd, GoogleDirection.MODE_DRIVING);
        mProgressDialog.setContent(R.string.getting_direction);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "onStart");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "onPause");
        mGoogleDirection.cancelAnimated();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS,"onResume");
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(Utils.TAG_ACTIVITY_DIRECTIONS, "onDestroy");
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    // Method to handle physical back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Call transition when physical back button pressed
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    // Method to set up map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabDirection:
                mProgressDialog.show();
                // Clear map first before redirecting location
                mMap.clear();
                mStart = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                getDirection(mStart);
                break;
        }
    }
}