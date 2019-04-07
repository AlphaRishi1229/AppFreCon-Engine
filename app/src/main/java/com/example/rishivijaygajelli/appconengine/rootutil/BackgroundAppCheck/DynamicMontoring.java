package com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

import com.example.rishivijaygajelli.appconengine.CPUActivity;
import com.example.rishivijaygajelli.appconengine.Services.SleepService;
import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.pranavpandey.android.dynamic.engine.model.DynamicAppInfo;
import com.pranavpandey.android.dynamic.engine.service.DynamicEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.example.rishivijaygajelli.appconengine.MainScreenActivity.CurrentFrequency.CUR_CPU_PATH;
import static com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util.toMHz;

public class DynamicMontoring extends DynamicEngine {

    String app_name = null;
    DynamicAppInfo dynamicAppInfo;
    public String max = null;
    String min = null;
    String sleep_max = null;
    String sleep_min = null;
    String unlock_max = null;
    String unlock_min = null;

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

        if (screenOff) {
            String cur_freq = Util.readOneLine(CUR_CPU_PATH);
            String curfreqfinal = toMHz(cur_freq);

            String max_freq = Util.readOneLine(CPUActivity.MAX_FREQ_PATH);
            String maxfreqfinal = toMHz(max_freq);

            String min_freq = Util.readOneLine(CPUActivity.MIN_FREQ_PATH);
            String minfreqfinal = toMHz(min_freq);
            Intent in = new Intent("intentKey2");
            in.putExtra("curfreq", curfreqfinal);
            in.putExtra("maxfreq", maxfreqfinal);
            in.putExtra("minfreq", minfreqfinal);

            //sendBroadcast(in);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

            SleepService.start(getBaseContext());
            File file = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine/CPU Frequency(Sleep).conf");
            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        String sleep_config = stringBuilder.toString();
                        String[] config = sleep_config.split("(?<=\\D)(?=\\d)");
                        sleep_max = config[0];
                        sleep_min = config[1];
                        SetFreq setFreq = new SetFreq(sleep_max, sleep_min);
                        setFreq.execute();
                    }
                    fileReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLockStateChange(boolean locked) {
        if (!locked) {
            SleepService.stop(getBaseContext());
            File file = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine/CPU Frequency.conf");
            if (file.exists()) {
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        String sleep_config = stringBuilder.toString();
                        String[] config = sleep_config.split("(?<=\\D)(?=\\d)");
                        unlock_max = config[0];
                        unlock_min = config[1];
                        SetFreq setFreq = new SetFreq(unlock_max, unlock_min);
                        setFreq.execute();
                    }
                    fileReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

        this.dynamicAppInfo = dynamicAppInfo;
        String appname = AppName();
        File f = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine/App Frequency-" + appname + ".conf");
        if (dynamicAppInfo != null)

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
                    // Util.setFreq(max, min);
                    SetFreq setFreq = new SetFreq(max, min);
                    setFreq.execute();
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

    public class SetFreq extends AsyncTask<Void, Void, Void> {
        public SetFreq(String maxfreq, String minfreq) {
            max = maxfreq;
            min = minfreq;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            Util.setFreq(max, min);
            return null;
        }
    }
}

