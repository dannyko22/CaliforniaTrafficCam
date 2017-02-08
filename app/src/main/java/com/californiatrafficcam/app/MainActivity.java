package com.californiatrafficcam.app;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends FragmentActivity {

    InterstitialAd interstitial;
    AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeAdNetwork();

        ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitial!=null && interstitial.isLoaded())
                {
                    interstitial.show();
                }
                startActivity(new Intent(MainActivity.this,CaliMapsActivity.class));
            }
        });

        ImageButton aboutmeButton = (ImageButton) findViewById(R.id.aboutmeButton);
        aboutmeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interstitial!=null && interstitial.isLoaded())
                {
                    interstitial.show();
                }
                startActivity(new Intent(MainActivity.this,about_me.class));
            }
        });

    }

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
