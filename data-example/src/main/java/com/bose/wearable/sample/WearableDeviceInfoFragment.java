package com.bose.wearable.sample;

//
//  ARDeviceInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/03/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

public class WearableDeviceInfoFragment extends BaseWearableDeviceInfoFragment {
    private Preference mMajorVersion;
    private Preference mMinorVersion;
    private Preference mProductId;
    private Preference mVariant;
    private Preference mTransmissionPeriod;
    private Preference mMaxPayload;
    private Preference mMaxSensors;
    private Preference mDeviceStatus;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.wearable_device_info, rootKey);

        mMajorVersion = findPreference("major_version");
        mMinorVersion = findPreference("minor_version");
        mProductId = findPreference("product_id");
        mVariant = findPreference("product_variant");
        mTransmissionPeriod = findPreference("transmission_period");
        mMaxPayload = findPreference("max_payload");
        mMaxSensors = findPreference("max_sensors");
        mDeviceStatus = findPreference("device_status");
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation info) {
        mMajorVersion.setSummary(String.valueOf(info.majorVersion()));
        mMinorVersion.setSummary(String.valueOf(info.minorVersion()));
        mProductId.setSummary(Integer.toString(info.productInfo().id()));
        mVariant.setSummary(Integer.toString(info.productInfo().variant()));

        mTransmissionPeriod.setSummary(String.valueOf(info.transmissionPeriod()));
        mMaxPayload.setSummary(String.valueOf(info.maxPayload()));
        mMaxSensors.setSummary(String.valueOf(info.maxActiveSensors()));
        mDeviceStatus.setSummary(info.deviceStatus().toString());
    }
}
