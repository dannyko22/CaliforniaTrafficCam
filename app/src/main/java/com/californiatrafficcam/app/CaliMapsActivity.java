package com.californiatrafficcam.app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

public class CaliMapsActivity extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    DataBaseHelper myDbHelper;
    ArrayList<CamData> camList;
    private UiSettings mapUISetting;
    Hashtable<String, Integer> markers;
    public ImageView urlImageView;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cali_maps);

        if (!isNetworkAvailable())
        {
            showAlertDialog();
        }

        camList = new ArrayList<CamData>();

        myDbHelper = new DataBaseHelper(this);
        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){

            throw sqle;
        }

        camList = myDbHelper.getCamData();

        markers = new Hashtable<String, Integer>();



        setUpMapIfNeeded();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(this);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(final Marker mark) {
                mark.showInfoWindow();
                return true;


            }
        });

        initializeAdNetwork();

    }


    public void showAlertDialog()
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Sorry... This app requires an Internet connection. :(");
        alertDialogBuilder.setNeutralButton("Exit the app", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                CaliMapsActivity.this.finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void initializeAdNetwork()
    {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            aboutMenuItem();
        } else if (id==R.id.rateme)
        {
            try {
                Uri uri = Uri.parse("market://details?id=com.californiatrafficcam.app");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.californiatrafficcam.app"
                        )));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void aboutMenuItem() {
        startActivity(new Intent(this,about_me.class));

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        int count = camList.size();
        String name;
        double latitude;
        double longitude;
        String species;
        LatLng locationLatLngSetup;
        boolean outside_california = false;
        CameraPosition cameraPosition;

        mapUISetting = mMap.getUiSettings();
        mapUISetting.setMyLocationButtonEnabled(true);
        mapUISetting.setTiltGesturesEnabled(false);
        mapUISetting.setRotateGesturesEnabled(false);
        mapUISetting.setCompassEnabled(true);

        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location myLocation = locationManager.getLastKnownLocation(provider);

        if (myLocation != null) {
            if (myLocation.getLatitude() > 32 && myLocation.getLatitude() < 42 && myLocation.getLongitude() > -125 && myLocation.getLongitude() < -113) {
                // your current location is within California
                locationLatLngSetup = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                outside_california = false;
            } else {
                // your current location is outside california
                locationLatLngSetup = new LatLng(35.948698, -120.302488);
                outside_california = true;
            }
        }
        else
        {
            // your current location is outside california
            locationLatLngSetup = new LatLng(35.948698, -120.30248);
            outside_california = true;
        }

        if (outside_california)
        {
            cameraPosition = new CameraPosition.Builder()
                    .target(locationLatLngSetup) // Sets the center of the map
                    .zoom(8)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera to north
                    .tilt(0)    // Sets the tilt of the camera to 0 degrees
                    .build();    // Creates a CameraPosition from the builder
        }
        else {
            cameraPosition = new CameraPosition.Builder()
                    .target(locationLatLngSetup) // Sets the center of the map
                    .zoom(11)                   // Sets the zoom
                    .bearing(0) // Sets the orientation of the camera to north
                    .tilt(0)    // Sets the tilt of the camera to 0 degrees
                    .build();    // Creates a CameraPosition from the builder
        }

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition));

        while (count != 0) {
            name = camList.get(count-1).getName();
            latitude = Double.parseDouble(camList.get(count-1).getLatitude());
            longitude = Double.parseDouble(camList.get(count-1).getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.carmarker));
            Marker marker = mMap.addMarker(markerOptions);
            markers.put(marker.getId(), count-1);
            count--;

        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        String url = camList.get(markers.get(marker.getId())).getURL();
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;
        Context mContext;
        Marker previousMarker = null;
        Marker currentMarker = null;
        boolean not_first_time_showing_info_window = false;
        Callback callback = null;

        public CustomInfoWindowAdapter(Context context) {
            view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContext = context;
        }

        @Override
        public View getInfoContents(Marker marker) {

            previousMarker = currentMarker;
            currentMarker = marker;

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            // Loader image - will be shown before loading image

            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            final String url = camList.get(markers.get(marker.getId())).getURL();
            urlImageView = ((ImageView) view.findViewById(R.id.trafficimage));


            if (not_first_time_showing_info_window==false & previousMarker==null) {
                // starting up app.  Enter this first if statement on first pass.
                not_first_time_showing_info_window = true;
                callback = new InfoWindowRefresher(marker);
                Picasso.with(CaliMapsActivity.this).load(url).into(urlImageView, callback);
            } else if (not_first_time_showing_info_window==true & previousMarker.getId().toString().compareTo(currentMarker.getId().toString()) != 0)
            {
                // enter this if statement when the user quickly clicks on another marker.
                // the user clicks on the first marker and then the url gets fetched.  but in the meantime, the user simultaneously clicks on another marker.
                // by clicking on another marker, the user stops the callback of the previous marker fetch.  so we need to re-initiate/re-start all the fetches.
                // so this is what is needed to quickly go to the next marker and get the url image and properly refreshes the info window.
                not_first_time_showing_info_window=true;
                callback = new InfoWindowRefresher(marker);
                Picasso.with(CaliMapsActivity.this).load(url).into(urlImageView,callback);
            } else if (not_first_time_showing_info_window==true) {
                // if the marker is clicked, and the user hasn't clicked on another marker, this is the default statement to go into.
                Picasso.with(CaliMapsActivity.this).load(url).into(urlImageView);
            } else if (not_first_time_showing_info_window==false & previousMarker.getId().toString().compareTo(currentMarker.getId().toString()) != 0) {
                // if the user clicks on a marker, and the url from the previous click was fetched properly, and callback was completed
                // and now the user wants to click on another marker, call this function.
                not_first_time_showing_info_window=true;
                callback = new InfoWindowRefresher(marker);
                Picasso.with(CaliMapsActivity.this).load(url).into(urlImageView,callback);
            }

            return view;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            return null;
        }

        //call back needed as the info window needs to be refreshed twice.
        private class InfoWindowRefresher implements com.squareup.picasso.Callback {
            private Marker markerToRefresh;

            private InfoWindowRefresher(Marker markerToRefresh) {
                this.markerToRefresh = markerToRefresh;
            }



            @Override
            public void onSuccess() {
                if (markerToRefresh != null  && markerToRefresh.isInfoWindowShown())
                {
                    markerToRefresh.showInfoWindow();
                    not_first_time_showing_info_window=false;
                    callback = null;
                }
            }

            @Override
            public void onError() {
            }
        }
    }

    public void displayInterstitial() {
        // If Ads are loaded, show Interstitial else show nothing.
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
    }


}
