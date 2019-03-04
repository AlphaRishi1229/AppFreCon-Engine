package com.example.rishivijaygajelli.appconengine;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rishivijaygajelli.appconengine.Services.ForegroundService;
import com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.CPUStateMonitor;
import com.topjohnwu.superuser.ContainerApp;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.rishivijaygajelli.appconengine.MainScreenActivity.CurrentFrequency.CUR_CPU_PATH;
import static com.example.rishivijaygajelli.appconengine.MainScreenActivity.CurrentFrequency.NUM_OF_CPUS_PATH;
import static com.example.rishivijaygajelli.appconengine.rootutil.CPUstates.CPUStateMonitor.PREF_OFFSETS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainScreenActivity extends AppCompatActivity {

    public static int curcpu = 0;
    public static ArrayList<String> mCPUon = new ArrayList<String>();
    CurrentFrequency cf = new CurrentFrequency();
    public int nCpus = cf.getNumOfCpus();
    SharedPreferences preferences;
    private TextView ui_total_state_time;
    private LinearLayout ui_states_view;
    private TextView ui_states_warning;
    private TextView ui_header_additional_states;
    private TextView ui_additional_states;
    private TextView ui_current_freq;
    private DrawerLayout drawer_main;
    private Switch service_switch;
    private Button btn_refresh;
    private CPUStateMonitor monitor = new CPUStateMonitor();
    private boolean mUpdatingData = false;

    private static String toString(long tSec) {
        long h = (long) Math.floor(tSec / (60 * 60));
        long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
        long s = tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10)
            sDur += "0";
        sDur += m + ":";
        if (s < 10)
            sDur += "0";
        sDur += s;

        return sDur;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Shell.rootAccess();


        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshData();
                                String cur_freq = cf.readOneLine(CUR_CPU_PATH);
                                String curfreqfinal = toMHz(cur_freq);

                                String max_freq = cf.readOneLine(CPUActivity.MAX_FREQ_PATH);
                                String maxfreqfinal = toMHz(max_freq);

                                String min_freq = cf.readOneLine(CPUActivity.MIN_FREQ_PATH);
                                String minfreqfinal = toMHz(min_freq);

                                Intent in = new Intent("intentKey");
                                in.putExtra("curfreq",curfreqfinal);
                                in.putExtra("maxfreq",maxfreqfinal);
                                in.putExtra("minfreq",minfreqfinal);

                                //sendBroadcast(in);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
                                ui_current_freq.setText(curfreqfinal);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        thread.start();


        ui_states_view = (LinearLayout) findViewById(R.id.ui_states_view);
        ui_total_state_time = (TextView) findViewById(R.id.ui_total_state_time);
        ui_states_warning = (TextView) findViewById(R.id.ui_states_warning);
        ui_header_additional_states = (TextView) findViewById(R.id.ui_header_additional_states);
        ui_current_freq = (TextView) findViewById(R.id.ui_current_freq);
        ui_additional_states = (TextView) findViewById(R.id.ui_additional_states);
        drawer_main = (DrawerLayout)findViewById(R.id.drawer_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                drawer_main.closeDrawers();

                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        drawer_main.closeDrawers();
                        break;

                    case R.id.nav_cpu:
                        Intent cpu1 = new Intent(MainScreenActivity.this, CPUActivity.class);
                        startActivity(cpu1);
                        break;

                    case R.id.nav_settings:
                        Intent cpu2 = new Intent(MainScreenActivity.this, CPUActivity.class);
                        startActivity(cpu2);
                        break;

                    case R.id.nav_about:
                        Intent cpu3 = new Intent(MainScreenActivity.this, CPUActivity.class);
                        startActivity(cpu3);
                        break;
                }
                return true;
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadOffsets();

        service_switch = findViewById(R.id.service_switch);
        service_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == TRUE)
                {
                    if(!needsUsageStatsPermission())
                    {
                        Toast.makeText(MainScreenActivity.this,"Permission Already Granted",Toast.LENGTH_LONG).show();
                        Toast.makeText(MainScreenActivity.this,"AppFreCon Service Started",Toast.LENGTH_LONG).show();
                        ForegroundService.start(getBaseContext());
                    }
                    else
                    {
                        requestUsageStatsPermission();
                    }
                }
                else if(isChecked == FALSE)
                {
                    ForegroundService.stop(getBaseContext());
                    Toast.makeText(MainScreenActivity.this,"AppFreCon Service Stopped",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean needsUsageStatsPermission() {
        return!hasUsageStatsPermission(this);
    }

    private void requestUsageStatsPermission() {
        if(!hasUsageStatsPermission(this)) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
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

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putInt("curcpu", curcpu);
    }

    @Override
    public void onResume() {
        refreshData();
        super.onResume();

    }

    public String toMHz(String mhzString) {
        if ((mhzString == null) || (mhzString.length() <= 0)) return "";
        else return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }

    public void refreshData() {
        if (!mUpdatingData) {
            new RefreshStateDataTask().execute((Void) null);
        }
    }

    public void updateView() {
        ui_states_view.removeAllViews();
        List<String> extraStates = new ArrayList<String>();
        for (CPUStateMonitor.CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                generateStateRow(state, ui_states_view);
            } else {
                if (state.freq == 0) {
                    extraStates.add("Deep Sleep");
                } else {
                    extraStates.add(state.freq / 1000 + " MHz");
                }
            }
        }
        if (monitor.getStates().size() == 0) {
            ui_states_warning.setVisibility(View.VISIBLE);
            //mHeaderTotalStateTime.setVisibility(View.GONE);
            ui_total_state_time.setVisibility(View.GONE);
            ui_states_view.setVisibility(View.GONE);
        }
        long totTime = monitor.getTotalStateTime() / 100;
        ui_total_state_time.setText(toString(totTime));

        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0)
                    str += ", ";
                str += s;
            }

            ui_additional_states.setVisibility(View.VISIBLE);
            ui_header_additional_states.setVisibility(View.VISIBLE);
            ui_additional_states.setText(str);

            // ui_current_freq.setText(cf.run());
        } else {
            ui_additional_states.setVisibility(View.GONE);
            ui_header_additional_states.setVisibility(View.GONE);
        }
    }

    private View generateStateRow(CPUStateMonitor.CpuState state, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.state_row, parent, false);

        float per = (float) state.duration * 100 / monitor.getTotalStateTime();
        String sPer = (int) per + "%";

        String sFreq;
        if (state.freq == 0) {
            sFreq = "Deep Sleep";
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        long tSec = state.duration / 100;
        String sDur = toString(tSec);

        TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
        TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
        TextView perText = (TextView) view.findViewById(R.id.ui_percentage_text);
        ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_bar);

        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int) per);

        parent.addView(view);
        return view;
    }

    public void loadOffsets() {
        String prefs = preferences.getString(PREF_OFFSETS, "");
        if (prefs == null || prefs.length() < 1) {
            return;
        }
        Map<Integer, Long> offsets = new HashMap<Integer, Long>();
        String[] sOffsets = prefs.split(",");
        for (String offset : sOffsets) {
            String[] parts = offset.split(" ");
            offsets.put(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
        }
        monitor.setOffsets(offsets);
    }

    public void saveOffsets() {
        SharedPreferences.Editor editor = preferences.edit();
        String str = "";
        for (Map.Entry<Integer, Long> entry : monitor.getOffsets().entrySet()) {
            str += entry.getKey() + " " + entry.getValue() + ",";
        }
        editor.putString(PREF_OFFSETS, str).commit();

    }

    protected class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... v) {
            try {
                monitor.updateStates();
            } catch (CPUStateMonitor.CPUStateMonitorException e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mUpdatingData = true;
        }

        @Override
        protected void onPostExecute(Void v) {
            try {
                updateView();
            } catch (Exception e) {

            }
            mUpdatingData = false;
        }
    }

    public class CurrentFrequency{

        public static final String NUM_OF_CPUS_PATH = "/sys/devices/system/cpu/present";
        public static final String CPU_ON_PATH = "/sys/devices/system/cpu/cpu0/online";
        public static final String CUR_CPU_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";


        //  public final String cur_freq_new = readOneLine(CUR_CPU_PATH);

        private boolean mInterrupt = false;
        private String onlist = "";


        public int getNumOfCpus() {
            int numOfCpu = 1;
            String numOfCpus = readOneLine(NUM_OF_CPUS_PATH);
            String[] cpuCount = numOfCpus.split("-");
            if (cpuCount.length > 1) {
                try {
                    int cpuStart = Integer.parseInt(cpuCount[0]);
                    int cpuEnd = Integer.parseInt(cpuCount[1]);
                    numOfCpu = cpuEnd - cpuStart + 1;
                    if (numOfCpu < 0) numOfCpu = 1;
                } catch (NumberFormatException ex) {
                    numOfCpu = 1;
                }
            }
            return numOfCpu;
        }

        public String readOneLine(String fname) {
            String line = null;
            if (new File(fname).exists()) {
                BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader(fname), 512);
                    try {
                        line = br.readLine();

                    } finally {
                        br.close();
                    }
                } catch (Exception e) {
                    //Log.e(TAG, "IO Exception when reading sys file", e);
                    // attempt to do magic!
                    return readFileViaShell(fname);
                    //return Shell.su("cat "+fname).exec().toString();
                }
            }
            return line;
        }




        public String readFileViaShell(String filePath) {

            String line = null;
            //return Shell.su("cat "+"$"+filePath).exec().toString();
       try
        {
            SuFile file =  new SuFile(filePath);
            SuFileInputStream fileInput = new SuFileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            BufferedReader buf = new BufferedReader (new InputStreamReader(fileInput, Charset.defaultCharset()));
            while((line =buf.readLine()) != null){
                stringBuilder.append(line);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MainScreenActivity.this,"Not reading",Toast.LENGTH_LONG).show();
        }
        return line;
    }
    }
}

