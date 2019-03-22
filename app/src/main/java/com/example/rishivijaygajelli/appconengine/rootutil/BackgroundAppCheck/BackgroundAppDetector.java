package com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.rishivijaygajelli.appconengine.MainScreenActivity;



public class BackgroundAppDetector implements Detector {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String getForegroundApp(final Context context) throws PackageManager.NameNotFoundException {
        if(!MainScreenActivity.hasUsageStatsPermission(context))
            return null;

        ApplicationInfo foregroundApp = null;
        String applicationName = null;


        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Service.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        final PackageManager pm = context.getApplicationContext().getPackageManager();
        UsageEvents usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time);
        UsageEvents.Event event = new UsageEvents.Event();
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
            if(event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                //foregroundApp = event.getPackageName() ;
                foregroundApp = pm.getApplicationInfo(event.getPackageName(),0);
               applicationName = (String) (foregroundApp != null ? pm.getApplicationLabel(foregroundApp) : "(unknown)");
            }
        }

        return applicationName;
    }
}