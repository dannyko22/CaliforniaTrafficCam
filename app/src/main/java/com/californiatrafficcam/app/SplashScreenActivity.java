package com.californiatrafficcam.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by Danny on 23/01/2017.
 */

public class SplashScreenActivity extends FragmentActivity {

    private static int SPLASH_TIME_OUT = 4000;
    InterstitialAd interstitial;
    AdRequest adRequest;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initializeAdNetwork();

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Intent i = new Intent(SplashScreenActivity.this, CaliMapsActivity.class);
                startActivity(i);

            }
        });

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                // Prepare an Interstitial Ad Listener

                Intent i = new Intent(SplashScreenActivity.this, CaliMapsActivity.class);
                if (interstitial!=null && interstitial.isLoaded()) {
                    interstitial.show();
                } else {
                    startActivity(i);
                }


                // close this activity
                finish();

            }
        }, SPLASH_TIME_OUT);
    }


//        Intent intent = new Intent(this, CaliMapsActivity.class);
//        startActivity(intent);
//        finish();


    private void initializeAdNetwork()
    {
        adRequest = new AdRequest.Builder().build();
        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(this);
        // Insert the Ad Unit ID
        interstitial.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitial.loadAd(adRequest);
    }

}