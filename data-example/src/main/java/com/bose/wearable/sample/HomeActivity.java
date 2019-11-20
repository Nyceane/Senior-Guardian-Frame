package com.bose.wearable.sample;

//
//  HomeActivity.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/15/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bose.wearable.BoseWearable;

public class HomeActivity extends AppCompatActivity implements HomeFragment.Listener {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that the minimum required API level is available
        if (Build.VERSION.SDK_INT < BoseWearable.MINIMUM_SUPPORTED_OS_VERSION) {
            Toast.makeText(this, getString(R.string.insufficient_api_level, BoseWearable.MINIMUM_SUPPORTED_OS_VERSION),
                Toast.LENGTH_LONG)
                .show();
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new HomeFragment())
                .commit();
        }
    }

    @Override
    public void onDeviceSelected(@NonNull final String deviceAddress) {
        startActivity(MainActivity.intentForDevice(this, deviceAddress));
    }

    @Override
    public void onSimulatedDeviceSelected() {
        startActivity(MainActivity.intentForSimulatedDevice(this));
    }
}
