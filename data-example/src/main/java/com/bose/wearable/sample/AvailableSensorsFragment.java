package com.bose.wearable.sample;

//
//  AvailableSensorsFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/04/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;

import com.bose.wearable.services.wearablesensor.SensorType;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

import java.util.Set;

public class AvailableSensorsFragment extends BaseWearableDeviceInfoFragment {
    private CheckBoxPreference mAccelerometer;
    private CheckBoxPreference mGyroscope;
    private CheckBoxPreference mRotationVector;
    private CheckBoxPreference mGameRotation;
    private CheckBoxPreference mOrientation;
    private CheckBoxPreference mMagnetometer;
    private CheckBoxPreference mUncalibratedMagnetometer;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.available_sensors, rootKey);

        mAccelerometer = findPreference("accelerometer");
        mGyroscope = findPreference("gyroscope");
        mRotationVector = findPreference("rotation_vector");
        mGameRotation = findPreference("game_rotation_vector");
        mOrientation = findPreference("orientation");
        mMagnetometer = findPreference("magnetometer");
        mUncalibratedMagnetometer = findPreference("uncalibrated_magnetometer");
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation deviceInformation) {
        final Set<SensorType> available = deviceInformation.availableSensors();

        mAccelerometer.setChecked(available.contains(SensorType.ACCELEROMETER));
        mGyroscope.setChecked(available.contains(SensorType.GYROSCOPE));
        mRotationVector.setChecked(available.contains(SensorType.ROTATION_VECTOR));
        mGameRotation.setChecked(available.contains(SensorType.GAME_ROTATION_VECTOR));
        mOrientation.setChecked(available.contains(SensorType.ORIENTATION));
        mMagnetometer.setChecked(available.contains(SensorType.MAGNETOMETER));
        mUncalibratedMagnetometer.setChecked(available.contains(SensorType.UNCALIBRATED_MAGNETOMETER));
    }
}
