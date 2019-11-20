package com.bose.ar.basic_example;

//
//  App.java
//  BoseWearable
//
//  Created by Tambet Ingo on 12/10/2018.
//  Copyright © 2018 Bose Corporation. All rights reserved.
//

import android.app.Application;
import android.os.Build;

import com.bose.wearable.BoseWearable;
import com.bose.wearable.Config;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= BoseWearable.MINIMUM_SUPPORTED_OS_VERSION) {
            BoseWearable.configure(this, new Config.Builder().build());
        }
    }
}
