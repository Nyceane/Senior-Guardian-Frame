package com.bose.wearable.sample;

//
//  AvailableGesturesFragment.java
//  BoseWearable
//
//  Created by Tambet Ingo on 10/04/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;

import com.bose.wearable.services.wearablesensor.GestureType;
import com.bose.wearable.services.wearablesensor.WearableDeviceInformation;

import java.util.Set;

public class AvailableGesturesFragment extends BaseWearableDeviceInfoFragment {
    private CheckBoxPreference mSingleTap;
    private CheckBoxPreference mDoubleTap;
    private CheckBoxPreference mHeadNod;
    private CheckBoxPreference mHeadShake;
    private CheckBoxPreference mTouchAndHold;
    private CheckBoxPreference mInput;
    private CheckBoxPreference mAffirmative;
    private CheckBoxPreference mNegative;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.available_gestures, rootKey);

        mSingleTap = findPreference("single_tap");
        mDoubleTap = findPreference("double_tap");
        mHeadNod = findPreference("head_nod");
        mHeadShake = findPreference("head_shake");
        mTouchAndHold = findPreference("touch_and_hold");
        mInput = findPreference("input_gesture");
        mAffirmative = findPreference("affirmative_gesture");
        mNegative = findPreference("negative_gesture");
    }

    protected void onDataUpdated(@NonNull final WearableDeviceInformation deviceInformation) {
        final Set<GestureType> available = deviceInformation.availableGestures();

        mSingleTap.setChecked(available.contains(GestureType.SINGLE_TAP));
        mDoubleTap.setChecked(available.contains(GestureType.DOUBLE_TAP));
        mHeadNod.setChecked(available.contains(GestureType.HEAD_NOD));
        mHeadShake.setChecked(available.contains(GestureType.HEAD_SHAKE));
        mTouchAndHold.setChecked(available.contains(GestureType.TOUCH_AND_HOLD));
        mInput.setChecked(available.contains(GestureType.INPUT));
        mAffirmative.setChecked(available.contains(GestureType.AFFIRMATIVE));
        mNegative.setChecked(available.contains(GestureType.NEGATIVE));
    }
}
