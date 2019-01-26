package com.example.rishivijaygajelli.appconengine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.Util;
import com.example.rishivijaygajelli.appconengine.rootutil.RootUtil;

import java.io.File;

public class CPUActivity extends AppCompatActivity {

    Spinner spn_app, spn_governor, spn_io;
    LinearLayout lcurcpu;
    TextView current_speed, max_speed_text, min_speed_text;
    SeekBar max_slider, min_slider;
    Toolbar toolbar;

    private int nCpus= Util.getNumOfCpus();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);

        current_speed = findViewById(R.id.current_speed);
        max_speed_text = findViewById(R.id.max_speed_text);
        min_speed_text = findViewById(R.id.min_speed_text);

        lcurcpu = findViewById(R.id.lcurcpu);
        max_slider = findViewById(R.id.max_slider);
        min_slider = findViewById(R.id.min_slider);
        toolbar = (Toolbar)findViewById(R.id.toolbar);



    }
}
