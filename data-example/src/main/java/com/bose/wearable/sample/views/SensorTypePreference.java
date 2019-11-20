package com.bose.wearable.sample.views;

//
//  SensorTypePreference.java
//  BoseWearable
//
//  Created by Tambet Ingo on 06/28/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import com.bose.wearable.sample.R;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.List;

public class SensorTypePreference extends ListPreference {
    @NonNull
    private SensorType mSensorType = SensorType.UNKNOWN;
    @Nullable
    private SamplePeriod mSamplePeriod;

    public SensorTypePreference(final Context context,
                                final AttributeSet attrs,
                                final int defStyleAttr,
                                final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SensorTypePreference(final Context context,
                                final AttributeSet attrs,
                                final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SensorTypePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorTypePreference(final Context context) {
        super(context);
        init();
    }

    private void init() {
        setPersistent(false);
        setWidgetLayoutResource(R.layout.pref_sensor_type);
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        final TextView textView = (TextView) holder.findViewById(R.id.text);
        textView.setText(getContext().getString(R.string.sample_period_ms, millis()));
    }

    public void sensorType(@NonNull final SensorType sensorType) {
        mSensorType = sensorType;

        setKey(String.valueOf(sensorType.value()));
        setTitle(sensorType.toString());
        setDialogTitle(getContext().getString(R.string.sample_period_dialog_title_one,
            sensorType.toString()));
    }

    public void samplePeriod(@Nullable final SamplePeriod samplePeriod) {
        mSamplePeriod = samplePeriod;
    }

    public void availableSamplePeriods(@NonNull final List<SamplePeriod> periods) {
        final Context context = getContext();
        final short millis = millis();
        final int periodCount = periods.size();

        int selected = -1;

        final String[] labels = new String[periodCount + 1];
        final String[] values = new String[periodCount + 1];

        for (int i = 0; i < periodCount; i++) {
            final SamplePeriod sp = periods.get(i);

            labels[i] = periodLabel(context, sp);
            values[i] = String.valueOf(sp.milliseconds());
            if (sp.milliseconds() == millis) {
                selected = i;
            }
        }

        labels[periodCount] = context.getString(R.string.sensor_config_disable_sensor,
            mSensorType.toString());
        values[periodCount] = "-1";

        setEntries(labels);
        setEntryValues(values);

        if (selected >= 0) {
            setValueIndex(selected);
        }
    }

    private short millis() {
        return mSamplePeriod != null ? mSamplePeriod.milliseconds() : 0;
    }

    private static String periodLabel(@NonNull final Context context,
                                      @NonNull final SamplePeriod samplePeriod) {
        switch (samplePeriod) {
            case _320_MS:
                return context.getString(R.string.sample_period_320);
            case _160_MS:
                return context.getString(R.string.sample_period_160);
            case _80_MS:
                return context.getString(R.string.sample_period_80);
            case _40_MS:
                return context.getString(R.string.sample_period_40);
            case _20_MS:
                return context.getString(R.string.sample_period_20);
            case _10_MS:
                return context.getString(R.string.sample_period_10);
            case _5_MS:
                return context.getString(R.string.sample_period_5);
            default:
                return "<unknown>";
        }
    }
}
