package com.bose.wearable.sample;

//
//  SensorConfListFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/18/2018.
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

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sample.views.SensorTypePreference;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorConfiguration;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SensorConfigFragment extends PreferenceFragmentCompat {
    private SessionViewModel mViewModel;
    private PreferenceGroup mSensorGroup;
    @NonNull
    private SensorInformation mSensorInfo = SensorInformation.EMPTY;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.sensor_conf, rootKey);

        mSensorGroup = findPreference("sensor_group");
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final String key = preference.getKey();

        if ("disable_all".equals(key)) {
            mViewModel.disableAllSensors();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableSensorInfo()
            .observe(this, info -> mSensorInfo = info);

        mViewModel.wearableSensorConfiguration()
            .observe(this, this::onSensorsRead);
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
        mViewModel.refreshSensorConfigurations();
    }

    private void onSensorsRead(@NonNull final SensorConfiguration sensorConfiguration) {
        mSensorGroup.removeAll();

        final Context context = requireContext();
        for (final SensorType sensorType : sensorConfiguration.allSensors()) {
            final SensorTypePreference pref = new SensorTypePreference(context);
            pref.sensorType(sensorType);
            pref.samplePeriod(sensorConfiguration.sensorSamplePeriod(sensorType));

            final List<SamplePeriod> periods = new ArrayList<>(mSensorInfo.availableSamplePeriods(sensorType));
            Collections.sort(periods, (a, b) -> a.milliseconds() > b.milliseconds() ? 1 : 0);
            pref.availableSamplePeriods(periods);

            pref.setOnPreferenceChangeListener((preference, newName) -> {
                final short newMillis = Short.valueOf((String) newName);
                mViewModel.enableSensor(sensorType, newMillis);
                return false;
            });

            mSensorGroup.addPreference(pref);
        }
    }
}
