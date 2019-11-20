package com.bose.wearable.sample;

//
//  DeviceInfoFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/31/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bose.blecore.deviceinformation.DeviceInformation;
import com.bose.blecore.util.Util;
import com.bose.wearable.sample.viewmodels.SessionViewModel;

public class DeviceInfoFragment extends PreferenceFragmentCompat {
    private SessionViewModel mViewModel;
    private Preference mSystemId;
    private Preference mModelNumber;
    private Preference mSerialNumber;
    private Preference mFirmwareRev;
    private Preference mHardwareRev;
    private Preference mSoftwareRev;
    private Preference mManufacturerName;
    private Preference mRegulatoryCert;
    private Preference mPnpId;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.device_info, rootKey);

        mSystemId = findPreference("system_id");
        mModelNumber = findPreference("model_number");
        mSerialNumber = findPreference("serial_number");
        mFirmwareRev = findPreference("firmware_rev");
        mHardwareRev = findPreference("hardware_rev");
        mSoftwareRev = findPreference("software_rev");
        mManufacturerName = findPreference("manufacturer_name");
        mRegulatoryCert = findPreference("regulatory_cert");
        mPnpId = findPreference("pnp_id");
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.monitorDeviceInfo()
            .observe(this, this::onDataLoaded);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                mViewModel.refreshDeviceInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDataLoaded(@NonNull final DeviceInformation info) {
        mSystemId.setSummary(Util.bytesToHexString(info.systemId()));
        mModelNumber.setSummary(info.modelNumber());
        mSerialNumber.setSummary(info.serialNumber());
        mFirmwareRev.setSummary(info.firmwareRevision());
        mHardwareRev.setSummary(info.hardwareRevision());
        mSoftwareRev.setSummary(info.softwareRevision());
        mManufacturerName.setSummary(info.manufacturerName());
        mRegulatoryCert.setSummary(Util.bytesToHexString(info.regulatoryCertifications()));
        mPnpId.setSummary(Util.bytesToHexString(info.pnpId()));
    }
}
