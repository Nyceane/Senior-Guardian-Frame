package com.bose.wearable.sample;

import android.app.Application;
import android.os.Build;

import com.bose.wearable.BoseWearable;
import com.bose.wearable.Config;
import com.bose.wearable.focus.FocusMode;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= BoseWearable.MINIMUM_SUPPORTED_OS_VERSION) {
            final Config config = new Config.Builder()
                .focusMode(focusMode())
                .build();

            BoseWearable.configure(this, config);
        }
    }

    private static FocusMode focusMode() {
        //noinspection ConstantConditions
        if ("MANUAL".equalsIgnoreCase(BuildConfig.FOCUS_MODE)) {
            return FocusMode.MANUAL;
        } else {
            return FocusMode.IGNORED;
        }
    }
}
