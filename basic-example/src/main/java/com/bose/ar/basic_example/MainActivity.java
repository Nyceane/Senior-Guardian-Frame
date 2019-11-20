package com.bose.ar.basic_example;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.bose.wearable.BoseWearable;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that the minimum required API level is available
        if (Build.VERSION.SDK_INT < BoseWearable.MINIMUM_SUPPORTED_OS_VERSION) {
            Toast.makeText(this, getString(R.string.insufficient_api_level, BoseWearable.MINIMUM_SUPPORTED_OS_VERSION),
                Toast.LENGTH_LONG)
                .show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new HomeFragment())
                .commit();
        }
    }
}
