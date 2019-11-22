package com.bose.wearable.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bose.wearable.sample.viewmodels.SessionViewModel;
import com.bose.wearable.sensordata.SensorValue;
import com.bose.wearable.services.wearablesensor.SamplePeriod;
import com.bose.wearable.services.wearablesensor.SensorType;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

public class FallFragment extends Fragment {

    // accelerometer vector
    private SessionViewModel mViewModel;
    private float mAccelLast, mAccel, mAccelCurrent, maxAccelSeen;
    private Boolean fallDetected = false;
    private float[] mGravity;
    public static float normalThreshold = 2,
            fallenThreshold = 1;
    private int movecount = 0;
    TextToSpeech t1;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.accelerometerData()
                .observe(this, this::onAccSensorData);

        mViewModel.sensorSamplePeriod(SamplePeriod._80_MS);
        mViewModel.enableSensor(SensorType.ACCELEROMETER, SamplePeriod._80_MS.milliseconds());

        t1=new TextToSpeech(this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });
    }


    private void onAccSensorData(@NonNull final SensorValue sensorValue) {
        final SensorType sensorType = sensorValue.sensorType();
        float[] values = new float[]{(float)sensorValue.vector().x(), (float)sensorValue.vector().y(), (float)sensorValue.vector().z()};
        //Log.e(sensorValue.sensorType().toString(), String.valueOf(values[0]) + "," + String.valueOf(values[1]) + "," +  String.valueOf(values[2]));

        switch (sensorType) {
            case ACCELEROMETER:
                double threshold = (fallDetected == true) ? fallenThreshold : normalThreshold;
                mGravity = values;
                mAccelLast = mAccelCurrent;
                mAccelCurrent = (float) Math.sqrt(mGravity[0] * mGravity[0] + mGravity[1] * mGravity[1] + mGravity[2] * mGravity[2]);
                float delta = mAccelCurrent - mAccelLast;
                mAccel = Math.abs(mAccel * 0.9f) + delta;
                if (mAccel > maxAccelSeen) {
                    maxAccelSeen = mAccel;
                    //string message = "Increased";
                }


                Log.d("sensor", "Sensor ServiceX: onChange mAccel=" + mAccel + " maxAccelSeen=" + maxAccelSeen + " threshold=" + threshold);
                if (mAccel > threshold) {
                    Log.e(sensorValue.sensorType().toString(), String.valueOf(values[0]) + "," + String.valueOf(values[1]) + "," + String.valueOf(values[2]));
                    maxAccelSeen = 0;
                    if ((fallDetected == true) && (mAccel > fallenThreshold)) {
                        Log.e("fall", "fall detected");
                    } else {
                        if ((fallDetected == false) && (mAccel > normalThreshold)) {
                            fallDetected = true;
                            Log.e("fall", "true fall detected");
                            t1.speak("Fall detected, please see if you can move", TextToSpeech.QUEUE_FLUSH, null, "txtFall");
                        }
                    }
                }

                if (fallDetected)
                {
                    if(mAccel < 0.02)
                    {
                        movecount++;
                        if(movecount >= 10)
                        {
                            //8 seconds
                            smsHelp();
                        }
                    }
                    else {
                        //reset
                        fallDetected = false;
                    }
                }

                break;
            case GYROSCOPE:
            case MAGNETOMETER:
            case ORIENTATION:
            default:
                break;
        }
    }

    private void smsHelp()
    {
        t1.speak("You are not moving, sending a text to your emergency contact", TextToSpeech.QUEUE_FLUSH, null, "txtFall");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String smsNumber = preferences.getString(this.getContext().getString(R.string.contact), "");
        if(!smsNumber.equalsIgnoreCase(""))
        {
            String helpMessage = "I have fallen and need help, sent by Senior Guardian Frames on Android";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(smsNumber,
                    null,
                    helpMessage,
                    null,
                    null);
            Toast.makeText(this.getContext(), "Your contact has been notified",
                    Toast.LENGTH_LONG).show();
        }

    }



}
