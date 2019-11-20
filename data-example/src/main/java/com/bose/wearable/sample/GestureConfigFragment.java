package com.bose.wearable.sample;

//
//  GestureConfigFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/25/2018.
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
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.services.wearablesensor.GestureConfiguration;
import com.bose.wearable.services.wearablesensor.GestureType;

import java.util.Collections;
import java.util.List;

public class GestureConfigFragment extends PreferenceFragmentCompat {
    private SessionViewModel mViewModel;
    private PreferenceGroup mGestureGroup;
    @NonNull
    private GestureConfiguration mGestureConf = GestureConfiguration.EMPTY;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.gesture_conf, rootKey);

        mGestureGroup = findPreference("gesture_group");
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final String key = preference.getKey();

        if ("enable_all".equals(key)) {
            enableAll();
            return true;
        } else if ("disable_all".equals(key)) {
            disableAll();
            return true;
        } else if (preference.getParent() == mGestureGroup) {
            final byte value = Byte.parseByte(preference.getKey());
            final GestureType gestureType = GestureType.fromData(value);
            set(gestureType, ((CheckBoxPreference) preference).isChecked());

            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.wearableGestureConfiguration()
            .observe(this, this::onGesturesRead);
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
        mViewModel.refreshGestureConfiguration();
    }

    private void onGesturesRead(@NonNull final GestureConfiguration gestureConfiguration) {
        mGestureConf = gestureConfiguration;

        final List<GestureType> list = gestureConfiguration.allGestures();
        Collections.sort(list, (a, b) -> a.value() > b.value() ? 1 : 0);
        final Context context = requireContext();

        final int groupCount = mGestureGroup.getPreferenceCount();
        if (groupCount > 0 && groupCount != list.size()) {
            mGestureGroup.removeAll();
        }

        for (final GestureType gesture : list) {
            final String key = String.valueOf(gesture.value());
            CheckBoxPreference pref = mGestureGroup.findPreference(key);
            if (pref == null) {
                pref = new CheckBoxPreference(context);
                pref.setKey(key);
                pref.setTitle(gesture.toString());
                pref.setPersistent(false);
                mGestureGroup.addPreference(pref);
            }

            pref.setChecked(gestureConfiguration.gestureEnabled(gesture));
        }
    }

    private void set(@NonNull final GestureType gestureType, final boolean enabled) {
        doChange(mGestureConf.gestureEnabled(gestureType, enabled));
    }

    private void enableAll() {
        doChange(mGestureConf.enableAll());
    }

    private void disableAll() {
        doChange(mGestureConf.disableAll());
    }

    private void doChange(@NonNull final GestureConfiguration newConf) {
        if (!newConf.equals(mGestureConf)) {
            mViewModel.changeGestureConfiguration(newConf);
        }
    }
}

