package com.appupdate.appupdateproject;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.appsonair.AppsOnAirServices;
import com.appsonair.UpdateCallBack;
import com.appsonair.ScreenshotDetectionDelegate;


public class MainActivity extends Activity {

    private ScreenshotDetectionDelegate screenshotDetectionDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get your appId from https://appsonair.com/
        AppsOnAirServices.setAppId("f79d23d0-c65e-4680-916b-513433049bd8", true);
        AppsOnAirServices.checkForAppUpdate(this, new UpdateCallBack() {
            @Override
            public void onSuccess(String response) {
                Log.e("mye", ""+response);
            }

            @Override
            public void onFailure(String message) {
                Log.e("mye", "onFailure"+message);

            }
        });

        // Initialize ScreenshotDetectionDelegate
        screenshotDetectionDelegate = new ScreenshotDetectionDelegate(this);

        // Start screenshot detection
        screenshotDetectionDelegate.startScreenshotDetection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop screenshot detection to avoid memory leaks
        screenshotDetectionDelegate.stopScreenshotDetection();
    }

}