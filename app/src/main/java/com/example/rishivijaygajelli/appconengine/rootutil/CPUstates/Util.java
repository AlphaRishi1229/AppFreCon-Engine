package com.example.rishivijaygajelli.appconengine.rootutil.CPUstates;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rishivijaygajelli.appconengine.CPUActivity;
import com.topjohnwu.superuser.ShellUtils;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuProcessFileInputStream;
import com.topjohnwu.superuser.io.SuProcessFileOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Util {

    private static final String TAG = "";
    static String NUM_OF_CPUS_PATH = "/sys/devices/system/cpu/present";


    public static int getNumOfCpus() {
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

    public static String readOneLine(String fname) {
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


    public static String toMHz(String mhzString) {
        if ((mhzString == null) || (mhzString.length() <= 0)) return "";
        else return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }


    public static String readFileViaShell(String filePath) {

        String line = null;
        //return Shell.su("cat "+"$"+filePath).exec().toString();
        try {
            SuFile file = new SuFile(filePath);
            SuProcessFileInputStream fileInput = new SuProcessFileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();

            BufferedReader buf = new BufferedReader(new InputStreamReader(fileInput, Charset.defaultCharset()));
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            // Toast.makeText(MainScreenActivity.this,"Not reading",Toast.LENGTH_LONG).show();
        }
        return line;
    }

    public static void writeMax(String maxfreq) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(maxfreq.getBytes(Charset.forName("UTF-8")));
        int cpus = getNumOfCpus();

        for (int i = 0; i < cpus; i++) {
            try {
                File f = new File(CPUActivity.MAX_FREQ_PATH.replace("cpu0", "cpu" + i));
                if (f.exists()) {
                    SuProcessFileOutputStream outputStream = new SuProcessFileOutputStream(f);
                    ShellUtils.pump(inputStream, outputStream);
                    outputStream.close();
                }
                if (!f.exists()) {
                    break;
                }
            } catch (Exception e) {

            }
        }

    }

    public static void writeMin(String minfreq) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(minfreq.getBytes(Charset.forName("UTF-8")));
        int cpus = getNumOfCpus();

        for (int i = 0; i < cpus; i++) {
            try {
                File f = new File(CPUActivity.MIN_FREQ_PATH.replace("cpu0", "cpu" + i));
                if (f.exists()) {
                    SuProcessFileOutputStream outputStream = new SuProcessFileOutputStream(f);
                    ShellUtils.pump(inputStream, outputStream);
                    outputStream.close();
                }
                if (!f.exists()) {
                    break;
                }
            } catch (Exception e) {

            }
        }

    }

    public static void setFreq(String max_freq, String min_freq) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeMax(max_freq);
                writeMin(min_freq);
            }


        }).start();

    }


    private static SharedPreferences getSharedPrefs(final Context context) {
        final String prefsName = "prefs_name";
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    public static boolean getSharedPref(final Context context, final int keyId, final boolean defValue) {
        return getSharedPref(context, context.getString(keyId), defValue);
    }

    public static boolean getSharedPref(final Context context, final String key, final boolean defValue) {
        boolean value = getSharedPrefs(context).getBoolean(key, defValue);
        Log.i(TAG, "Reading bool pref \"" + key + "\" = " + value);
        return value;
    }

    public static boolean setSharedPref(final Context context, final int keyId, final String value) {
        return setSharedPref(context, context.getString(keyId), value);
    }

    public static boolean setSharedPref(final Context context, final String key, final String value) {
        Log.i(TAG, "Setting string pref \"" + key + "\" = " + value);
        return getSharedPrefs(context)
                .edit()
                .putString(key, value)
                .commit();
    }

    public static boolean setSharedPref(final Context context, final int keyId, final boolean value) {
        return setSharedPref(context, context.getString(keyId), value);
    }

    public static boolean setSharedPref(final Context context, final String key, final boolean value) {
        Log.i(TAG, "Setting bool pref \"" + key + "\" = " + value);
        return getSharedPrefs(context)
                .edit()
                .putBoolean(key, value)
                .commit();
    }
}
