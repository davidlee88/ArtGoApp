package com.ArtGo.ArtGoApp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ArtGo.ArtGoApp.utils.RestClient;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.toolbox.ImageLoader;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.nineoldandroids.view.ViewHelper;
import com.ArtGo.ArtGoApp.R;
import com.ArtGo.ArtGoApp.utils.DBHelperLocations;
import com.ArtGo.ArtGoApp.utils.ImageLoaderFromDrawable;
import com.ArtGo.ArtGoApp.utils.MySingleton;
import com.ArtGo.ArtGoApp.utils.Utils;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission.CALL_PHONE;

/**
 * ActivityDetail is created to display location detail.
 * Created using ActivityBase.
 */
public class ActivityDetail extends ActivityBase implements
        ObservableScrollViewCallbacks,
        View.OnClickListener,
        OnMapReadyCallback {

    private static final String TAG = ActivityDetail.class.getSimpleName();

    // Create view objects
    private ImageView mImgLocationImage;
    private Toolbar mToolbarView;
    private ObservableScrollView mScrollView;
    private int mParallaxImageHeight;
    private HtmlTextView mTxtDescription;
    private TextView mTxtLocationName, mTxtLocationCategory, mTxtLocationDistance;
    private CircleProgressBar mPrgLoading;
    private AdView mAdView;
    private GoogleMap mMap;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    // Create class objects and variables that required in this class
    private ImageLoaderFromDrawable mImageLoaderFromDrawable;
    private ImageLoader mImageLoader;
    private DBHelperLocations mDBhelper;
    private boolean mIsAdmobVisible;
    private String mSelectedId;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private static final int REQUEST_CALL_PHONE = 0;

    // Create variables to store location data
    private String mLocationName, mLocationAddress, mLocationImage,
            mLocationLongitude, mLocationLatitude, mLocationDescription, mLocationPhone,
            mLocationWebsite, mLocationMarker, mLocationCategory,mLocationAuthor,mLocationDate,mLocationStructure;

    // Create float array variable to store distance between location and user position
    private float[] mDistance = new float[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get dimension value from dimens.xml
        Resources mRes      = getResources();
        int mImageWidth     = mRes.getDimensionPixelSize(R.dimen.flexible_space_image_height);
        int mImageHeight    = mRes.getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mParallaxImageHeight= mRes.getDimensionPixelSize(R.dimen.parallax_image_height);

        // Create database helper object, image loader object, and utils object
        mDBhelper       = new DBHelperLocations(this);
        mImageLoader    = MySingleton.getInstance(this).getImageLoader();
        mImageLoaderFromDrawable = new ImageLoaderFromDrawable(this, mImageWidth, mImageHeight);

        // Get location id that passed from previous activity
        mSelectedId     = getIntent().getStringExtra(Utils.ARG_LOCATION_ID);

        // Connect view objects and view ids in xml
        mImgLocationImage         = (ImageView) findViewById(R.id.image);
        mPrgLoading               = (CircleProgressBar) findViewById(R.id.prgLoading);
        mToolbarView              = (Toolbar) findViewById(R.id.toolbar);
        mTxtDescription           = (HtmlTextView) findViewById(R.id.txtDescription);
        mTxtLocationName          = (TextView) findViewById(R.id.txtLocationName);
        mTxtLocationCategory      = (TextView) findViewById(R.id.txtLocationCategory);
        mTxtLocationDistance      = (TextView) findViewById(R.id.txtLocationDistance);
        //mAdView                   = (AdView) findViewById(R.id.adView);
        /*LinearLayout mBtnCall     = (LinearLayout) findViewById(R.id.btnCall);
        LinearLayout mBtnWebsite  = (LinearLayout) findViewById(R.id.btnWebsite);
        LinearLayout mBtnShare    = (LinearLayout) findViewById(R.id.btnShare);*/
        LinearLayout mBtnShare1    = (LinearLayout) findViewById(R.id.btnShare1);
        LinearLayout mBtnWebsite  = (LinearLayout) findViewById(R.id.btnWebsite);
        ButtonFlat mFlatDirection = (ButtonFlat) findViewById(R.id.flatDirection);
        mScrollView               = (ObservableScrollView) findViewById(R.id.scroll);
        SupportMapFragment mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        FacebookSdk.sdkInitialize(this);
        // Call onMapReady to set up map
        mMapFragment.getMapAsync(this);

        // Set toolbar as actionbar, set color to transparent, and add navigation up button
        setSupportActionBar(mToolbarView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().
                getColor(R.color.primary_color)));
        // Set listener for some view objects
        mScrollView.setScrollViewCallbacks(this);
        mBtnWebsite.setOnClickListener(this);
        mBtnShare1.setOnClickListener(this);
        mFlatDirection.setOnClickListener(this);
        mImgLocationImage.setOnClickListener(this);

        // Set progress circle loading color
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        //Set Animation Duration
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        // Get admob visibility value
        //mIsAdmobVisible = Utils.admobVisibility(mAdView, Utils.IS_ADMOB_VISIBLE);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        //mSharebutton.setOnClickListener(this);
        // Load ad in background using asynctask class
        //new SyncShowAd(mAdView).execute();

        // Check databases
        checkDatabase();

        // Get location data from database in background using asyntask class
        new SyncGetLocations().execute();
        //

    }

    // Method to show snackbar
    void showSnackBar(String message){
        new SnackBar.Builder(this)
                .withMessage(message)
                .show();
    }

    // Method to open Google Maps
    public void openGoogleMaps(){
        // Check whether Google App is installed in user device
        if(isGoogleMapsInstalled())
        {
            // If installed, send latitude and longitude value to the app
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+mLocationLatitude+","+
                    mLocationLongitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else
        {
            // If not installed, display dialog to ask user to install Google Maps app
            new MaterialDialog.Builder(this)
                .content(getString(R.string.google_maps_installation))
                .positiveText(getString(R.string.install))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        try {
                            // Open Google Maps app page in Google Play app
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=com.google.android.apps.maps")
                            ));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(
                                    // Open Google Maps app page in Google Play web
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" +
                                            "com.google.android.apps.maps")
                            ));
                        }
                    }
                })
                .show();
        }
    }

    // Method to set up map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                new LatLng(Utils.ARG_DEFAULT_LATITUDE, Utils.ARG_DEFAULT_LONGITUDE),
                5);
        mMap.moveCamera(cameraUpdate);
    }

    // Asynctask class to handle load data from database in background
    public class SyncGetLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // When data still retrieve from database display loading view
            // and hide other view
            mPrgLoading.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
            mToolbarView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Get data from database
            getLocationDataFromRest(mSelectedId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //set button style when there is no website information
            LinearLayout mBtnWebsite  = (LinearLayout) findViewById(R.id.btnWebsite);
            if(mLocationWebsite.equals("null")){
                mBtnWebsite.setAlpha(.5f);
            }
            // When finished retrieve data from database, display data to the views
            if(mLocationAuthor.equals("null")||mLocationStructure.equals("null")||mLocationDate.equals("null")){
                mTxtDescription.setHtmlFromString("<strong>" + getString(R.string.address) + ":</strong>  " +
                        "<em>" + mLocationAddress + "</em>" +
                        "<br /><br /><strong>Description:</strong>" + mLocationDescription, true);
            }else {
                mTxtDescription.setHtmlFromString("<strong>" + getString(R.string.address) + ":</strong>  " +
                        "<em>" + mLocationAddress + "</em><br /><strong>" + getString(R.string.author) + ":</strong>  <em>" + mLocationAuthor + "</em><br />" +
                        "<strong>" + getString(R.string.date) + ":</strong>  <em>" + mLocationDate + "</em><br /><strong>" + getString(R.string.structure) + ":</strong>  <em>" + mLocationStructure + "</em><br /><br /><strong>Description:</strong>" + mLocationDescription, true);
            }
            mTxtLocationName.setText(mLocationName);
            mTxtLocationCategory.setText(mLocationCategory);
            String mFinalDistance = String.format("%.2f", (mDistance[0] / 1000)) + " " +
                    getString(R.string.km);
            mTxtLocationDistance.setText(mFinalDistance);

            if(mLocationImage.toLowerCase().contains("http")){
                mImageLoader.get(mLocationImage,
                        com.android.volley.toolbox.ImageLoader.getImageListener(mImgLocationImage,
                                R.mipmap.empty_photo, R.mipmap.empty_photo));

            } else {
                int image = getResources().getIdentifier(mLocationImage, "drawable",
                        getPackageName());
                mImageLoaderFromDrawable.loadBitmap(image, mImgLocationImage);
            }


            int marker = getResources().getIdentifier(mLocationMarker, "mipmap", getPackageName());
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(mLocationLatitude),
                            Double.valueOf(mLocationLongitude)))
                    .icon(BitmapDescriptorFactory.fromResource(marker))
                    .snippet(mLocationAddress)
                    .title(mLocationName));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(Double.valueOf(mLocationLatitude),
                            Double.valueOf(mLocationLongitude)),
                    14);
            mMap.animateCamera(cameraUpdate);

            // Hide loading view and display other views
            mPrgLoading.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            mToolbarView.setVisibility(View.VISIBLE);

        }
    }

    // Asynctask class to load admob in background
    public class SyncShowAd extends AsyncTask<Void, Void, Void>{

        AdView ad;
        AdRequest adRequest, interstitialAdRequest;
        InterstitialAd interstitialAd;
        int interstitialTrigger;

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

                // When interstitialTrigger equals ARG_TRIGGER_VALUE, display interstitial ad
                interstitialAd = new InterstitialAd(ActivityDetail.this);
                interstitialAd.setAdUnitId(ActivityDetail.this.getResources().getString(R.string.interstitial_ad_unit_id));
                interstitialTrigger = Utils.loadPreferences(getApplicationContext(),
                        Utils.ARG_TRIGGER);
                if(interstitialTrigger == Utils.ARG_TRIGGER_VALUE) {
                    if(Utils.IS_ADMOB_IN_DEBUG) {
                        interstitialAdRequest = new AdRequest.Builder()
                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                .build();
                    }else {
                        interstitialAdRequest = new AdRequest.Builder().build();
                    }
                    Utils.savePreferences(getApplicationContext(),Utils.ARG_TRIGGER, 0);
                }else{
                    Utils.savePreferences(getApplicationContext(),Utils.ARG_TRIGGER,
                            (interstitialTrigger+1));
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
                if (interstitialTrigger == Utils.ARG_TRIGGER_VALUE) {
                    // Start loading the ad
                    interstitialAd.loadAd(interstitialAdRequest);
                    // Set the AdListener
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            if (interstitialAd.isLoaded()) {
                                interstitialAd.show();
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {}

                        @Override
                        public void onAdClosed() {}
                    });
                }
            }

        }
    }

    // Method to retrieve data from database
    /*public void getLocationDataFromDatabase(String id) {
        ArrayList<Object> row = mDBhelper.getLocationDetailById(id);

        mLocationName           = row.get(0).toString();
        mLocationAddress        = row.get(1).toString();
        mLocationDescription    = row.get(2).toString();
        mLocationImage          = row.get(3).toString();
        mLocationLatitude       = row.get(4).toString();
        mLocationLongitude      = row.get(5).toString();
        mLocationDescription    = row.get(6).toString();
        mLocationPhone          = row.get(7).toString();
        mLocationWebsite        = row.get(8).toString();
        mLocationMarker         = row.get(9).toString();
        mLocationCategory       = row.get(10).toString();

        // Get distance between user position and location
        Location.distanceBetween(Double.valueOf(row.get(4).toString()),
                Double.valueOf(row.get(5).toString()),
                ActivityHome.mCurrentLatitude, ActivityHome.mCurrentLongitude, mDistance);

    }*/

    public void getLocationDataFromRest(String id) {
        JSONObject row = RestClient.getLocationDetailById(id);
        try {
            mLocationName = row.get("name").toString();
            mLocationAddress = row.get("address").toString();
            mLocationDescription = row.get("description").toString();
            mLocationImage = row.get("image").toString();
            String[] parts = row.get("geolocation").toString().split(",");
            String part1 = parts[0].replace("(","");
            String part2 = parts[1].replace(")","");
            mLocationLatitude = part1;
            mLocationLongitude = part2;
            mLocationPhone = "";
            mLocationWebsite = row.get("website").toString();
            mLocationMarker = row.get("marker").toString();
            mLocationCategory = row.get("typename").toString();
            mLocationAuthor = row.get("author").toString();
            String[] dates = row.get("date").toString().split("-");
            String year = dates[0];
            mLocationDate = year;
            mLocationStructure = row.get("structure").toString();

            // Get distance between user position and location
            Location.distanceBetween(Double.valueOf(mLocationLatitude),
                    Double.valueOf(mLocationLongitude),
                    ActivityHome.mCurrentLatitude, ActivityHome.mCurrentLongitude, mDistance);
        }catch (Exception e){

        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.primary_color);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(mImgLocationImage, scrollY / 2);
        // When scroll reach top screen, than display location name
        // on toolbar, else hide them
        if(alpha == 1){
            mToolbarView.setTitle(mLocationName);
        }else{
            mToolbarView.setTitle("");
        }
    }

    // Method to check database
    private void checkDatabase(){
        // Create object of database helpers
        mDBhelper = new DBHelperLocations(this);

        // Create database
        try {
            mDBhelper.createDataBase();
        }catch(IOException ioe){
            throw new Error("Unable to create database");
        }

        // Open database
        mDBhelper.openDataBase();
    }

    // Method to check whether Google Maps app is installed in user device
    public boolean isGoogleMapsInstalled()
    {
        try
        {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    "com.google.android.apps.maps", 0 );
            Log.d(Utils.TAG_PONGODEV + TAG,"info= "+info);
            return true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnWebsite:
                // If website address is not available display snackbar and set button grey,
                // else open browser to access website
                v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.image_click));
                LinearLayout mBtnWebsite  = (LinearLayout) findViewById(R.id.btnWebsite);
                if(mLocationWebsite.equals("null")){
                    mBtnWebsite.setClickable(false);
                    showSnackBar(getString(R.string.no_website_available));
                }else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(mLocationWebsite.trim()));
                    startActivity(browserIntent);
                }
                break;
            /*case R.id.btnCall:
                makeACall();
                break;*/
            case R.id.flatDirection:
                new MaterialDialog.Builder(this)
                .title(R.string.open_with)
                .items(R.array.app_list)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which,
                                            CharSequence text) {
                        switch (which) {
                            case 0:
                                // Send location latitude and longitude to Google Maps app
                                openGoogleMaps();
                                break;
                            case 1:
                                // Or use ArtGo to get direction from user position to the location
                                Intent directionIntent = new Intent(ActivityDetail.this, ActivityDirection.class);
                                directionIntent.putExtra(Utils.ARG_LOCATION_NAME, mLocationName);
                                directionIntent.putExtra(Utils.ARG_LOCATION_ADDRESSES, mLocationAddress);
                                directionIntent.putExtra(Utils.ARG_LOCATION_LATITUDE, mLocationLatitude);
                                directionIntent.putExtra(Utils.ARG_LOCATION_LONGITUDE, mLocationLongitude);
                                directionIntent.putExtra(Utils.ARG_LOCATION_MARKER, mLocationMarker);
                                startActivity(directionIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                                break;
                        }
                    }
                }).show();
                break;
            case R.id.image:
                if(mLocationImage.toLowerCase().contains("http")){
                    Bitmap bitmap = mImageLoader.get(mLocationImage,
                            com.android.volley.toolbox.ImageLoader.getImageListener(mImgLocationImage,
                                    R.mipmap.empty_photo, R.mipmap.empty_photo)).getBitmap();
                    zoomImageFromThumbInternet(mImgLocationImage,bitmap);

                } else {
                    int image = getResources().getIdentifier(mLocationImage, "drawable",
                            getPackageName());
                    zoomImageFromThumbLocal(mImgLocationImage, image);
                }
                //mScrollView.setVisibility(View.INVISIBLE);
                break;
            case R.id.btnShare1:
                v.startAnimation(AnimationUtils.loadAnimation(this,R.anim.image_click));
                sharePhotoToFB();
                break;
        }
    }

    private void zoomImageFromThumbInternet(final ImageView thumbView, Bitmap bitMap) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(bitMap);


        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.INVISIBLE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    private void zoomImageFromThumbLocal(final ImageView thumbView, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.INVISIBLE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }

    private void makeACall(){
        // If phone number is not available display snackbar,
        // else make a phone call
        if(mLocationPhone.equals("-")){
            showSnackBar(getString(R.string.no_phone_available));
        }else {
            if (!mayRequestCallPhone()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 8) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + mLocationPhone));
                try{
                    startActivity(callIntent);
                }catch (SecurityException e){
                    Log.d(Utils.TAG_PONGODEV + TAG,""+e.toString());
                }
                Log.d(Utils.TAG_PONGODEV + TAG, "start calling");
            }
        }
    }
    // Implement permissions requests on apps that target API level 23 or higher, and are
    // running on a device that's running Android 6.0 (API level 23) or higher.
    // If the device or the app's targetSdkVersion is 22 or lower, the system prompts the user
    // to grant all dangerous permissions when they install or update the app.
    private boolean mayRequestCallPhone() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // If already choose deny once
        if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
            askPermissionDialog();
        } else {
            requestPermissions(new String[]{CALL_PHONE}, REQUEST_CALL_PHONE);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // Call Phone-related task you need to do.
                makeACall();
                Log.d(Utils.TAG_PONGODEV + TAG, "Request Call Phone Allowed");
            } else {
                // permission was not granted
                if (getApplicationContext() == null) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
                        makeACall();
                    } else {
                        permissionSettingDialog();
                    }
                }
            }
        }
    }

    //Facebook share
    private void sharePhotoToFB() {
        if (ShareDialog.canShow(SharePhotoContent.class)) {


            if(mLocationImage.toLowerCase().contains("http")){
                Bitmap bitmap = mImageLoader.get(mLocationImage,
                        com.android.volley.toolbox.ImageLoader.getImageListener(mImgLocationImage,
                                R.mipmap.empty_photo, R.mipmap.empty_photo)).getBitmap();

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .setCaption("Nice ArtWork To Share!")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            } else {
                int imageRe = getResources().getIdentifier(mLocationImage, "drawable",
                        getPackageName());
                Bitmap image = BitmapFactory.decodeResource(getResources(),imageRe);
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .setCaption("Nice ArtWork To Share!")
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
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
            .content(R.string.request_call_phone)
            .positiveText(R.string.open_permission)
            .negativeText(android.R.string.cancel)
            .cancelable(false)
            .callback(new MaterialDialog.ButtonCallback() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onPositive(MaterialDialog dialog) {
                    requestPermissions(new String[]{CALL_PHONE}, REQUEST_CALL_PHONE);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    // Close dialog when Cancel button clicked
                    dialog.dismiss();
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
            .positiveColorRes(R.color.accent_color)
            .negativeColorRes(R.color.accent_color)
            .title(R.string.permission)
            .content(R.string.request_call_permission)
            .positiveText(R.string.open_setting)
            .negativeText(android.R.string.cancel)
            .cancelable(false)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getApplicationContext().
                            getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                @Override
                public void onNegative(MaterialDialog dialog) {
                    // Close dialog when Cancel button clicked
                    dialog.dismiss();
                }
            }).build();
        // Show dialog
        dialog.show();
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }


    // Method to handle physical back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Call transition when physical back button pressed
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    //FaceBook Call back
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
