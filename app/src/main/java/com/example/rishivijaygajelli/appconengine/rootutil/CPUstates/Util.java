package com.example.rishivijaygajelli.appconengine.rootutil.CPUstates;

import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Util {

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




    public static String readFileViaShell(String filePath) {

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
            // Toast.makeText(MainScreenActivity.this,"Not reading",Toast.LENGTH_LONG).show();
        }
        return line;
    }

}
