package com.bose.ar.basic_example;

//
//  HomeFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/04/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bose.blecore.DeviceException;
import com.bose.blecore.Logger;
import com.bose.blecore.ScanError;
import com.bose.bosewearableui.DeviceConnectorActivity;
import com.bose.wearable.BoseWearable;

import java.util.ArrayList;
import java.util.List;

@TargetApi(BoseWearable.MINIMUM_SUPPORTED_OS_VERSION)
public class HomeFragment extends Fragment {
    private static final int REQUEST_CODE_CONNECTOR = 1;
    private static final int AUTO_CONNECT_TIMEOUT = 5;
    private static final String PREF_AUTO_CONNECT_ENABLED = "auto-connect-enabled";

    private final List<View> mActionableViews = new ArrayList<>();
    private HomeViewModel mViewModel;
    private SwitchCompat mAutoConnectSwitch;
    @Nullable
    private View mProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button searchButton = view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> onSearchClicked());

        mAutoConnectSwitch = view.findViewById(R.id.autoConnectSwitch);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mAutoConnectSwitch.setChecked(prefs.getBoolean(PREF_AUTO_CONNECT_ENABLED, true));
        mAutoConnectSwitch.setOnCheckedChangeListener((compoundButton, enabled) -> {
            prefs.edit()
                .putBoolean(PREF_AUTO_CONNECT_ENABLED, enabled)
                .apply();
        });

        final Button simulatedDeviceButton = view.findViewById(R.id.simulatedDeviceButton);
        simulatedDeviceButton.setOnClickListener(v -> onSimulatedDeviceClicked());

        final TextView versionText = view.findViewById(R.id.versionText);
        versionText.setText(getString(R.string.version_name, BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE));

        mProgressBar = requireActivity().findViewById(R.id.progressbar);

        mActionableViews.add(searchButton);
        mActionableViews.add(mAutoConnectSwitch);
        mActionableViews.add(simulatedDeviceButton);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this)
            .get(HomeViewModel.class);

        mViewModel.errors()
            .observe(this, this::onError);

        mViewModel.state()
            .observe(this, this::onStateChanged);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        final ConnectionState state = mViewModel.state().getValue();
        if (state instanceof ConnectionState.Connecting) {
            inflater.inflate(R.menu.connecting_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cancel_action:
                mViewModel.reset();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CONNECTOR:
                if (resultCode == Activity.RESULT_OK) {
                    final String deviceAddress = data != null ? data.getStringExtra(DeviceConnectorActivity.CONNECTED_DEVICE) : null;
                    if (deviceAddress != null) {
                        onDeviceSelected(deviceAddress);
                    } else {
                        showNoDeviceError();
                    }
                } else if (resultCode == DeviceConnectorActivity.RESULT_SCAN_ERROR) {
                    final ScanError scanError = (ScanError) data.getSerializableExtra(DeviceConnectorActivity.FAILURE_REASON);
                    showScanError(scanError);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void onSearchClicked() {
        if (mAutoConnectSwitch.isChecked() && mViewModel.hasPreviouslyConnectedDevice()) {
            mViewModel.reconnect();
        } else {
            showDeviceConnector();
        }
    }

    private void showDeviceConnector() {
        final int autoConnectTimeout = mAutoConnectSwitch.isChecked() ? AUTO_CONNECT_TIMEOUT : 0;
        final Intent intent = DeviceConnectorActivity.newIntent(requireContext(), autoConnectTimeout,
            MainViewModel.sensorIntent(), MainViewModel.gestureIntent());

        startActivityForResult(intent, REQUEST_CODE_CONNECTOR);
    }

    private void onError(final Event<DeviceException> event) {
        final DeviceException e = event.get();
        if (e != null) {
            showError(e.getMessage());
        }
    }

    private void onStateChanged(final ConnectionState state) {
        if (state == ConnectionState.IDLE) {
            busy(false);
        } else if (state instanceof ConnectionState.Connecting) {
            busy(true);
        } else if (state instanceof ConnectionState.Connected) {
            busy(false);
            onDeviceSelected(((ConnectionState.Connected) state).destination().bluetoothDevice().getAddress());
            mViewModel.reset();
        }
    }

    private void busy(final boolean isBusy) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(isBusy ? View.VISIBLE : View.GONE);
        }
        for (final View view : mActionableViews) {
            view.setEnabled(!isBusy);
        }

        final Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void onSimulatedDeviceClicked() {
        final Bundle args = new Bundle();
        args.putBoolean(MainFragment.ARG_USE_SIMULATED_DEVICE, true);
        navigateToDeviceFragment(args);
    }

    private void onDeviceSelected(@NonNull final String deviceAddress) {
        final Bundle args = new Bundle();
        args.putString(MainFragment.ARG_DEVICE_ADDRESS, deviceAddress);
        navigateToDeviceFragment(args);
    }

    private void navigateToDeviceFragment(@NonNull final Bundle args) {
        final MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, fragment)
                .commit();
    }

    private void showNoDeviceError() {
        final Context context = getContext();
        if (context != null) {
            Toast.makeText(context, getString(R.string.no_device_selected),
                Toast.LENGTH_LONG)
                .show();
        }
    }

    private void showScanError(@NonNull final ScanError error) {
        final Context context = getContext();
        if (context == null) {
            Logger.e(Logger.Topic.DISCOVERY, "Scan failed with " + error);
            return;
        }

        final String reasonStr;
        switch (error) {
            case ALREADY_STARTED:
                reasonStr = context.getString(R.string.scan_error_already_started);
                break;
            case INTERNAL_ERROR:
                reasonStr = context.getString(R.string.scan_error_internal);
                break;
            case PERMISSION_DENIED:
                reasonStr = context.getString(R.string.scan_error_permission_denied);
                break;
            case BLUETOOTH_DISABLED:
                reasonStr = context.getString(R.string.scan_error_bluetooth_disabled);
                break;
            case FEATURE_UNSUPPORTED:
                reasonStr = context.getString(R.string.scan_error_feature_unsupported);
                break;
            case APPLICATION_REGISTRATION_FAILED:
                reasonStr = context.getString(R.string.scan_error_application_registration_failed);
                break;
            case UNKNOWN:
            default:
                reasonStr = context.getString(R.string.scan_error_unknown);
                break;
        }

        showError(context.getString(R.string.scan_failed, reasonStr));
    }

    private void showError(@NonNull final String message) {
        final Context context = getContext();
        if (context == null) {
            Logger.e(Logger.Topic.DISCOVERY, "Scan failed with " + message);
            return;
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG)
            .show();
    }
}
