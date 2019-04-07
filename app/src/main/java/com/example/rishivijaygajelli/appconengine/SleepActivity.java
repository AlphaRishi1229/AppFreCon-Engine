package com.example.rishivijaygajelli.appconengine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class SleepActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    public static final String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String STEPS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    TextView max_speed_sleep, min_speed_sleep;
    Button btn_save_sleep;
    SeekBar max_slider_sleep, min_slider_sleep;
    Toolbar toolbar;
    int max_cpu_array, min_cpu_array;
    int cpuFileMaxFinal, cpuFileMinFinal;
    String app = null;
    String cpuMaxFinal, cpuMinFinal = null;
    String cpuMax, cpuMin, cpuFileMax, cpuFileMin = null;
    String current_max = "";
    String current_min = "";
    String[] cpu;
    private DrawerLayout drawer_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        String cpu_steps = Util.readOneLine(STEPS_PATH);
        cpu = CPUActivity.getFrequencies(cpu_steps);
        int mFrequenciesNum = cpu.length - 1;

        max_speed_sleep = findViewById(R.id.max_speed_sleep);
        min_speed_sleep = findViewById(R.id.min_speed_sleep);
        btn_save_sleep = findViewById(R.id.btn_save_sleep);
        max_slider_sleep = findViewById(R.id.max_slider_sleep);
        max_slider_sleep.setOnSeekBarChangeListener(this);
        max_slider_sleep.setMax(mFrequenciesNum);
        min_slider_sleep = findViewById(R.id.min_slider_sleep);
        min_slider_sleep.setOnSeekBarChangeListener(this);
        min_slider_sleep.setMax(mFrequenciesNum);
        toolbar = findViewById(R.id.toolbar);
        drawer_main = findViewById(R.id.drawer_main3);

        cpuMax = Util.readOneLine(MAX_FREQ_PATH);
        cpuMaxFinal = Util.toMHz(cpuMax);
        max_speed_sleep.setText(cpuMaxFinal);

        cpuMin = Util.readOneLine(MIN_FREQ_PATH);
        cpuMinFinal = Util.toMHz(cpuMin);
        min_speed_sleep.setText(cpuMinFinal);

        for (int i = 0; i < cpu.length; i++) {
            if (cpuMax.equals(cpu[i])) {
                max_cpu_array = i;
                max_slider_sleep.setProgress(max_cpu_array);
            } else if (cpuMin.equals(cpu[i])) {
                min_cpu_array = i;
                min_slider_sleep.setProgress(min_cpu_array);
            }
        }

        btn_save_sleep.setOnClickListener(v -> {
            cpuFileMax = max_speed_sleep.getText().toString().replace(" MHz", "");
            cpuFileMaxFinal = Integer.parseInt(cpuFileMax) * 1000;
            cpuFileMin = min_speed_sleep.getText().toString().replace(" MHz", "");
            cpuFileMinFinal = Integer.parseInt(cpuFileMin) * 1000;

            writeSleepFreqtoFile(cpuFileMaxFinal, cpuFileMinFinal);
        });

        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        NavigationView navigationView = findViewById(R.id.nav_view3);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer_main.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        Intent cpu1 = new Intent(SleepActivity.this, MainScreenActivity.class);
                        startActivity(cpu1);
                        break;

                    case R.id.nav_cpu:
                        Intent cpu4 = new Intent(SleepActivity.this, CPUActivity.class);
                        startActivity(cpu4);
                        break;

                    case R.id.nav_settings:
                        drawer_main.closeDrawers();
                        break;

                    case R.id.nav_sleep:
                        drawer_main.closeDrawers();
                        break;

                    case R.id.nav_about:
                        drawer_main.closeDrawers();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer_main.openDrawer(GravityCompat.START);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void writeSleepFreqtoFile(int max_speed, int min_speed) {
        this.cpuFileMaxFinal = max_speed;
        this.cpuFileMinFinal = min_speed;
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine");
            if (!folder.exists()) {
                folder.mkdirs();
                System.out.println("Directory created");
            }
            File file = new File(Environment.getExternalStorageDirectory() + "/AppFreCon Engine/CPU Frequency(Sleep).conf");
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("File created");
            }
            if (file.exists()) {
                file.delete();
                file.createNewFile();
                FileOutputStream fout = new FileOutputStream(file.getAbsoluteFile(), true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fout);
                myOutWriter.write(max_speed + " " + min_speed + "\n");
                myOutWriter.flush();
            }

            if (max_speed < min_speed) {
                Toast.makeText(SleepActivity.this, "Min speed is greater than max", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.max_slider_sleep:
                    setMaxSpeed(progress);
                    break;
                case R.id.min_slider_sleep:
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
        int maxSliderProgress = max_slider_sleep.getProgress();
        if (progress <= maxSliderProgress) {
            max_slider_sleep.setProgress(progress);
            max_speed_sleep.setText(Util.toMHz(current_max));
        }
    }

    public void setMinSpeed(int progress) {
        current_min = cpu[progress];
        int minSliderProgress = min_slider_sleep.getProgress();
        if (progress <= minSliderProgress) {
            min_slider_sleep.setProgress(progress);
            min_speed_sleep.setText(Util.toMHz(current_min));
        }
    }

}
