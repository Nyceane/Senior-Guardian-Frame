package com.bose.wearable.sample;

//
//  DevicePropertiesFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 03/29/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.bmap.AnrInformation;
import com.bose.wearable.services.bmap.AnrMode;
import com.bose.wearable.services.bmap.CncValue;
import com.bose.wearable.wearabledevice.DeviceProperties;

import java.util.List;

public class DevicePropertiesFragment extends PreferenceFragmentCompat {
    private SessionViewModel mViewModel;
    private Preference mProtocolVersion;
    private Preference mAuthSupported;
    private Preference mGuid;
    private EditTextPreference mProductName;
    private Preference mBatteryLevel;
    private ListPreference mCnc;
    private ListPreference mAnr;
    @Nullable
    private DeviceProperties mProperties;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.device_properties, rootKey);

        mProtocolVersion = findPreference("protocol_version");
        mAuthSupported = findPreference("auth_supported");
        mGuid = findPreference("guid");
        mProductName = findPreference("product_name");
        mBatteryLevel = findPreference("battery_level");
        mCnc = findPreference("cnc");
        mAnr = findPreference("anr");

        mProductName.setOnPreferenceChangeListener((preference, newName) -> {
            updateName((String) newName);
            return false;
        });

        mCnc.setOnPreferenceChangeListener(((preference, newValue) -> {
            final int step = Integer.parseInt((String) newValue);
            if (step < 0) {
                final CncValue current = mProperties != null ? mProperties.cnc() : null;
                if (current != null) {
                    updateCnc(current.currentStep(), false);
                }
            } else {
                updateCnc(step, true);
            }
            return false;
        }));

        mAnr.setOnPreferenceChangeListener(((preference, newValue) -> {
            final byte b = Byte.parseByte((String) newValue);
            final AnrMode mode = AnrMode.parse(b);
            updateAnr(mode);
            return false;
        }));
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.monitorDeviceProperties()
            .observe(this, this::onDataLoaded);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        inflater.inflate(R.menu.device_properties, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_content:
                mViewModel.refreshDeviceProperties();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onDataLoaded(@NonNull final DeviceProperties deviceProperties) {
        mProperties = deviceProperties;

        mProtocolVersion.setSummary(deviceProperties.protocolVersion());
        mAuthSupported.setSummary(getString(deviceProperties.authenticationSupported() ? R.string.boolean_yes : R.string.boolean_no));
        mProductName.setSummary(deviceProperties.productName());
        mBatteryLevel.setSummary(deviceProperties.batteryLevel() + "%");

        final String guid = deviceProperties.guid();
        mGuid.setSummary(guid != null ? guid : getString(R.string.not_available));

        final CncValue cnc = deviceProperties.cnc();
        if (cnc != null && cnc.steps() > 0) {
            if (cnc.enabled()) {
                mCnc.setSummary(String.valueOf(cnc.steps() - (cnc.currentStep() + 1)));
            } else {
                mCnc.setSummary(getString(R.string.cnc_disabled));
            }

            final int steps = cnc.steps();
            final String[] labels = new String[steps + 1];
            final String[] values = new String[steps + 1];
            for (int i = 0; i < steps; i++) {
                labels[i] = String.valueOf(steps - (i + 1));
                values[i] = String.valueOf(i);
            }
            labels[0] = labels[0] + " (Max)";
            labels[steps - 1] = labels[steps - 1] + " (Min)";
            labels[steps] = "Disabled";
            values[steps] = "-1";

            mCnc.setEntries(labels);
            mCnc.setEntryValues(values);
            mCnc.setValueIndex(cnc.currentStep());
            mCnc.setSelectable(true);
        } else {
            mCnc.setSummary(getString(R.string.not_available));
            mCnc.setSelectable(false);
        }

        final AnrInformation anrInfo = deviceProperties.anr();
        if (anrInfo != null) {
            mAnr.setSummary(anrInfo.current().toString());

            final List<AnrMode> supported = anrInfo.supported();
            final String[] labels = new String[supported.size()];
            final String[] values = new String[supported.size()];
            int selectedIndex = -1;
            for (int i = 0; i < supported.size(); i++) {
                final AnrMode mode = supported.get(i);
                labels[i] = mode.toString();
                values[i] = String.valueOf(mode.value());
                if (anrInfo.current() == mode) {
                    selectedIndex = i;
                }
            }
            mAnr.setEntries(labels);
            mAnr.setEntryValues(values);
            if (selectedIndex >= 0) {
                mAnr.setValueIndex(selectedIndex);
            }
        } else {
            mAnr.setSummary(getString(R.string.not_available));
            mAnr.setSelectable(false);
        }
    }

    private void updateName(@Nullable final String newName) {
        if (newName != null && !newName.isEmpty()) {
            mViewModel.changeProductName(newName);
        }
    }

    private void updateCnc(final int newLevel,
                           final boolean enabled) {
        final CncValue current = mProperties != null ? mProperties.cnc() : null;
        if (current != null && (current.currentStep() != newLevel || current.enabled() != enabled)) {
            mViewModel.changeCnc(newLevel, enabled);
        }
    }

    private void updateAnr(@Nullable final AnrMode mode) {
        if (mode == null) {
            return;
        }
        final AnrInformation anr = mProperties != null ? mProperties.anr() : null;
        if (anr == null) {
            return;
        }

        if (anr.current().equals(mode)) {
            return;
        }

        mViewModel.changeAnr(mode);
    }
}
