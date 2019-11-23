package com.bose.wearable.sample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bose.wearable.sample.viewmodels.GestureEvent;
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
    private int laycount = 0;
    TextToSpeech t1;

    ImageView iconPerson;
    ImageView iconStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iconPerson = view.findViewById(R.id.iconPerson);
        iconStatus = view.findViewById(R.id.iconStatus);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(requireActivity()).get(SessionViewModel.class);
        mViewModel.accelerometerData()
                .observe(this, this::onAccSensorData);

        t1=new TextToSpeech(this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        mViewModel.sensorSamplePeriod(SamplePeriod._80_MS);
        mViewModel.enableSensor(SensorType.ACCELEROMETER, SamplePeriod._80_MS.milliseconds());

        mViewModel.gestureEvents()
                .observe(this, event -> {
                    final GestureEvent gestureEvent = event.get();
                    if (gestureEvent != null && gestureEvent.gestureData().type().name().equalsIgnoreCase("DOUBLE_TAP")) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                        String phoneNumber = preferences.getString(this.getContext().getString(R.string.contact), "");
                        if(!phoneNumber.equalsIgnoreCase(""))
                        {
                            t1.speak("Calling Guardian", TextToSpeech.QUEUE_FLUSH, null, "txtFall");

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                                    startActivity(intent);
                                }

                            }, 1500); // 5000ms delay
                        }
                    }
                    else if(gestureEvent != null && gestureEvent.gestureData().type().name().equalsIgnoreCase("NEGATIVE")) {

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
                            iconPerson.setImageResource(R.drawable.fell);
                            iconStatus.setImageResource(R.drawable.warning);
                        }
                    }
                }

                if (fallDetected)
                {
                    if(mAccel < 0.1)
                    {
                        laycount++;
                        if(laycount >= 100 && movecount <50)
                        {
                            //8 seconds
                            smsHelp();
                        }
                    }
                    else {
                        //reset
                        movecount++;
                        if(movecount >= 50)
                        {
                            movecount = 0;
                            laycount = 0;
                            fallDetected = false;
                            t1.speak("It seems you are ok, you can double tap to make emergency phone call", TextToSpeech.QUEUE_FLUSH, null, "txtFall");
                            iconPerson.setImageResource(R.drawable.standing);
                            iconStatus.setImageResource(R.drawable.check);
                        }

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
        movecount = 0;
        laycount = 0;
        fallDetected = false;

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



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                iconPerson.setImageResource(R.drawable.standing);
                iconStatus.setImageResource(R.drawable.check);
            }

        }, 5000); // 5000ms delay

    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        t1.shutdown();
    }

}
