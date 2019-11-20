package com.bose.wearable.sample;

//
//  SensorInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/16/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bose.wearable.impl.Range;
import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorInformation;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;
import java.util.Set;

@SuppressWarnings("PMD.EmptyCatchBlock")
public class SensorInfoFragment extends PreferenceFragmentCompat {
    public static final String ARG_SENSOR_TYPE = "sensor-type";

    private SessionViewModel mViewModel;
    private SensorType mSensorType;

    private Preference mScaledValue;
    private Preference mRawValue;
    private Preference mSampleLen;
    private CheckBoxPreference mPeriod320;
    private CheckBoxPreference mPeriod160;
    private CheckBoxPreference mPeriod80;
    private CheckBoxPreference mPeriod40;
    private CheckBoxPreference mPeriod20;
    private CheckBoxPreference mPeriod10;
    private CheckBoxPreference mPeriod5;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.sensor_info, rootKey);

        final Bundle args = getArguments();
        if (args != null) {
            mSensorType = (SensorType) args.getSerializable(ARG_SENSOR_TYPE);
        }

        if (mSensorType == null) {
            throw new IllegalArgumentException();
        }

        final Preference typePref = findPreference("sensor_type");
        typePref.setSummary(mSensorType.toString());

        mScaledValue = findPreference("scaled_range");
        mRawValue = findPreference("raw_range");
        mSampleLen = findPreference("sample_length");
        mPeriod320 = findPreference("sample_period_320");
        mPeriod160 = findPreference("sample_period_160");
        mPeriod80 = findPreference("sample_period_80");
        mPeriod40 = findPreference("sample_period_40");
        mPeriod20 = findPreference("sample_period_20");
        mPeriod10 = findPreference("sample_period_10");
        mPeriod5 = findPreference("sample_period_5");
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
        final Range<Short> scaledRange = sensorInformation.scaledValueRange(mSensorType);
        if (scaledRange != null) {
            mScaledValue.setSummary(String.format(Locale.US, "%d .. %d",
                scaledRange.lower(), scaledRange.upper()));
        }

        final Range<Short> rawRange = sensorInformation.rawValueRange(mSensorType);
        if (rawRange != null) {
            mRawValue.setSummary(String.format(Locale.US, "%d .. %d",
                rawRange.lower(), rawRange.upper()));
        }

        mSampleLen.setSummary(String.valueOf(sensorInformation.sampleLength(mSensorType)));

        final Set<SamplePeriod> sp = sensorInformation.availableSamplePeriods(mSensorType);
        mPeriod320.setChecked(sp.contains(SamplePeriod._320_MS));
        mPeriod160.setChecked(sp.contains(SamplePeriod._160_MS));
        mPeriod80.setChecked(sp.contains(SamplePeriod._80_MS));
        mPeriod40.setChecked(sp.contains(SamplePeriod._40_MS));
        mPeriod20.setChecked(sp.contains(SamplePeriod._20_MS));
        mPeriod10.setChecked(sp.contains(SamplePeriod._10_MS));
        mPeriod5.setChecked(sp.contains(SamplePeriod._5_MS));
    }
}
