package com.bose.wearable.sample;

//
//  SensorInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;

public class SensorInfoListFragment extends PreferenceFragmentCompat {
    private PreferenceGroup mSensorGroup;
    private SessionViewModel mViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(requireContext());
        mSensorGroup = screen;
        setPreferenceScreen(screen);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableSensorInfo()
            .observe(this, this::onSensorInfoRead);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                refreshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshData() {
        mViewModel.refreshWearableSensorInformation();
    }

    private void onSensorInfoRead(@NonNull final SensorInformation sensorInformation) {
        mSensorGroup.removeAll();

        final Context context = requireContext();
        for (final SensorType sensorType : sensorInformation.availableSensors()) {
            final Preference pref = new Preference(context);
            pref.setKey(String.format(Locale.US, "sensor_info_%d", sensorType.value()));
            pref.setTitle(sensorType.toString());
            pref.setWidgetLayoutResource(R.layout.pref_subsection);
            pref.setFragment("com.bose.wearable.sample.SensorInfoFragment");
            mSensorGroup.addPreference(pref);
        }
    }
}
