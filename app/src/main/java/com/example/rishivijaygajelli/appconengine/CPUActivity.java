package com.example.rishivijaygajelli.appconengine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.example.rishivijaygajelli.appconengine.rootutil.RootUtil;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CPUActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    Spinner spn_app, spn_governor;
    LinearLayout lcurcpu;
    TextView current_speed, max_speed_text, min_speed_text;
    SeekBar max_slider, min_slider;
    Toolbar toolbar;
    Button btn_save_profile;

    SharedPreferences mPreferences;

    String current_max = "";
    String current_min = "";

    public static final String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String STEPS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String GOVERNORS_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";

    private static final String TAG = "";
    public static ArrayList<String> mCurGovernor = new ArrayList<String>();
    public static ArrayList<String> mCurIO = new ArrayList<String>();
    public static ArrayList<String> mMaxFreqSetting = new ArrayList<String>();
    public static ArrayList<String> mMinFreqSetting = new ArrayList<String>();
    public static ArrayList<String> mCPUon = new ArrayList<String>();
    public String[] mAvailableFrequencies = new String[0];
    public Context c;
    Util util = new Util();
    int nCpus = util.getNumOfCpus();
    String[] cpu;

    int max_cpu_array, min_cpu_array;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String cpu_steps = util.readOneLine(STEPS_PATH);
        cpu = getFrequencies(cpu_steps);
        int mFrequenciesNum = cpu.length - 1;

        current_speed = findViewById(R.id.current_speed);
        max_speed_text = findViewById(R.id.max_speed_text);
        min_speed_text = findViewById(R.id.min_speed_text);

        lcurcpu = findViewById(R.id.lcurcpu);
        max_slider = findViewById(R.id.max_slider);
        max_slider.setOnSeekBarChangeListener(this);
        max_slider.setMax(mFrequenciesNum);

        min_slider = findViewById(R.id.min_slider);
        min_slider.setOnSeekBarChangeListener(this);
        min_slider.setMax(mFrequenciesNum);

        toolbar = (Toolbar)findViewById(R.id.toolbar);

        String cpuMax = util.readOneLine(MAX_FREQ_PATH);
        String cpuMaxFinal = util.toMHz(cpuMax);
        max_speed_text.setText(cpuMaxFinal);

        String cpuMin = util.readOneLine(MIN_FREQ_PATH);
        String cpuMinFinal = util.toMHz(cpuMin);
        min_speed_text.setText(cpuMinFinal);

        for(int i = 0; i < cpu.length; i++)
        {
            if (cpuMax.equals(cpu[i])) {
                max_cpu_array = i;
                max_slider.setProgress(max_cpu_array);
            }
            else if (cpuMin.equals(cpu[i]))
            {
                min_cpu_array = i;
                min_slider.setProgress(min_cpu_array);
            }
        }

        spn_app = findViewById(R.id.spn_app);
        List<String> list_app = new ArrayList<String>();
        list_app.add(".All Apps (Overall Device)");
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if( pm.getLaunchIntentForPackage(packageInfo.packageName) != null ){
                String title = pm.getApplicationLabel(packageInfo).toString();
                list_app.add(title);

            }
        }
        Collections.sort(list_app);
        ArrayAdapter<String> appAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list_app);
        spn_app.setAdapter(appAdapter);


        spn_governor = findViewById(R.id.spn_governor);
        String[] mAvailableGovernors = util.readOneLine(GOVERNORS_LIST_PATH).split(" ");
        String cur_governor = util.readOneLine(GOVERNOR_PATH);
        int in = 0;
        for(int i=0; i<mAvailableGovernors.length;i++)
        {
            if(cur_governor.equals(mAvailableGovernors[i]))
            {
                in=i;
            }
        }
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String mAvailableGovernor : mAvailableGovernors) {
            governorAdapter.add(mAvailableGovernor.trim());
        }
        spn_governor.setAdapter(governorAdapter);
        spn_governor.setSelection(in);


        btn_save_profile = findViewById(R.id.btn_save_profile);
        btn_save_profile.setOnClickListener(v -> {

            Util.setFreq(current_max,current_min);

        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()){
                case R.id.max_slider:
                    setMaxSpeed(progress);
                    break;
                case R.id.min_slider:
                    setMinSpeed(progress);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setMaxSpeed(int progress) {
        current_max = cpu[progress];
        int maxSliderProgress = max_slider.getProgress();
        if (progress <= maxSliderProgress) {
           max_slider.setProgress(progress);
            max_speed_text.setText(util.toMHz(current_max));
        }
    }

    public void setMinSpeed(int progress) {
        current_min = cpu[progress];
        int minSliderProgress = min_slider.getProgress();
        if (progress <= minSliderProgress) {
            min_slider.setProgress(progress);
            min_speed_text.setText(util.toMHz(current_min));
        }
    }

    public static String[] getFrequencies(String content)
    {
        String[] frequencyHz = content.split("\\s+");
        ArrayList<String> frequencies = new ArrayList<>(Arrays.asList(frequencyHz));
        return frequencies.toArray(new String[0]);
    }

}
