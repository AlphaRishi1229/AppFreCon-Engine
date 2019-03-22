package com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck;

import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.pranavpandey.android.dynamic.engine.model.DynamicAppInfo;
import com.pranavpandey.android.dynamic.engine.service.DynamicEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import androidx.annotation.Nullable;

public class DynamicMontoring extends DynamicEngine {

    String app_name = null;
    DynamicAppInfo dynamicAppInfo;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        setAppMonitorTask(true);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onInitialize(boolean charging, boolean headset, boolean docked) {

    }

    @Override
    public void onCallStateChange(boolean call) {

    }

    @Override
    public void onScreenStateChange(boolean screenOff) {

    }

    @Override
    public void onLockStateChange(boolean locked) {

    }

    @Override
    public void onHeadsetStateChange(boolean connected) {

    }

    @Override
    public void onChargingStateChange(boolean charging) {

    }

    @Override
    public void onDockStateChange(boolean docked) {

    }

    @Override
    public void onAppChange(@Nullable DynamicAppInfo dynamicAppInfo) {
        File f = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine/App Frequency.conf");
        String max = null;
        String min = null;
        this.dynamicAppInfo = dynamicAppInfo;
        String appname = AppName();
        if (dynamicAppInfo != null)
            Toast.makeText(this, dynamicAppInfo.getLabel(), Toast.LENGTH_LONG).show();

        try {
            FileReader fileReader = new FileReader(f);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(appname)) {
                    stringBuffer.append(line);
                    String app_config = stringBuffer.toString();
                    String[] config = app_config.split("(?<=\\D)(?=\\d)");
                    String nme = config[0];
                    max = config[1];
                    min = config[2];
                    Toast.makeText(this, "App Found" + "\n" + max + "\n" + min, Toast.LENGTH_LONG).show();
                    Util.setFreq(max, min);
                }
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String AppName() {
        app_name = dynamicAppInfo.getLabel();
        return app_name;
    }

    @Override
    public void onPackageUpdated(@Nullable DynamicAppInfo dynamicAppInfo, boolean newPackage) {

    }

    @Override
    public void onPackageRemoved(@Nullable String packageName) {

    }
}

