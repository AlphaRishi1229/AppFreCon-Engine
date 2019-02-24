package com.example.rishivijaygajelli.appconengine.rootutil.CPUstates;

import com.example.rishivijaygajelli.appconengine.CPUActivity;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;
import com.topjohnwu.superuser.io.SuFileOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Util {

    private static final String TAG = "";
    static String NUM_OF_CPUS_PATH = "/sys/devices/system/cpu/present";


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


    public String toMHz(String mhzString) {
        if ((mhzString == null) || (mhzString.length() <= 0)) return "";
        else return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }


    public static String readFileViaShell(String filePath) {

        String line = null;
        //return Shell.su("cat "+"$"+filePath).exec().toString();
        try {
            SuFile file = new SuFile(filePath);
            SuFileInputStream fileInput = new SuFileInputStream(file);
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

    public static boolean setFreq(String max_freq, String min_freq) {
       ByteArrayInputStream inputStream = new ByteArrayInputStream(max_freq.getBytes(Charset.forName("UTF-8")));
       ByteArrayInputStream inputStream1 = new ByteArrayInputStream(min_freq.getBytes(Charset.forName("UTF-8")));

       SuFileOutputStream outputStream;
        SuFileOutputStream outputStream1;
        try {
            if (max_freq != null) {
                int cpus = 0;
                while (true) {
                    SuFile f = new SuFile(CPUActivity.MAX_FREQ_PATH.replace("cpu0", "cpu" + cpus));
                    SuFile f1 = new SuFile(CPUActivity.MIN_FREQ_PATH.replace("cpu0", "cpu" + cpus));

                    outputStream = new SuFileOutputStream(f);
                    outputStream1 = new SuFileOutputStream(f1);

                    ShellUtils.pump(inputStream, outputStream);
                    ShellUtils.pump(inputStream1, outputStream1);

                    if (!f.exists()) {
                        break;
                    }
                    cpus++;
                }

            }
        } catch (Exception ex) {
        }
        return true;
    }

}
