package com.example.rishivijaygajelli.appconengine;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.example.rishivijaygajelli.appconengine.rootutil.RootUtil;

import java.io.File;
import java.util.ArrayList;

public class CPUActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    public static final String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    Spinner spn_app, spn_governor, spn_io;
    LinearLayout lcurcpu;
    TextView current_speed, max_speed_text, min_speed_text;
    SeekBar max_slider, min_slider;
    Toolbar toolbar;
    public static final String MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String STEPS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
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

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);

        String cpu_steps = util.readOneLine(STEPS_PATH);
        cpu = getFrequencies(cpu_steps);
        final int mFrequenciesNum = cpu.length - 1;

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
        final String current = cpu[progress];
        int maxSliderProgress = max_slider.getProgress();
        if (progress <= maxSliderProgress) {
           max_slider.setProgress(progress);
            max_speed_text.setText(util.toMHz(current));
        }
    }

    public void setMinSpeed(int progress) {
        final String current = cpu[progress];
        int minSliderProgress = min_slider.getProgress();
        if (progress <= minSliderProgress) {
            min_slider.setProgress(progress);
            min_speed_text.setText(util.toMHz(current));
        }
    }

    private String[] getFrequencies(String content)
    {
        ArrayList<String> frequencies = new ArrayList<>();
        String[] frequencyHz = content.split("\\s+");
        for(int i = 0; i < frequencyHz.length ; i++)
        {
            frequencies.add(frequencyHz[i]);
        }
        return frequencies.toArray(new String[0]);
    }
}
