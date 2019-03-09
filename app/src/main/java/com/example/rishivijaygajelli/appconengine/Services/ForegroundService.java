package com.example.rishivijaygajelli.appconengine.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import com.example.rishivijaygajelli.appconengine.R;
import com.example.rishivijaygajelli.appconengine.rootutil.BackgroundAppCheck.AppChecker;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ForegroundService extends Service {

    NotificationChannel notificationChannel;
    String channelId = "1";
    String curfreq = null;
    String maxfreq = null;
    String minfreq = null;

    private final static int NOTIFICATION_ID = 1234;
    private final static String STOP_SERVICE = ForegroundService.class.getPackage()+".stop";

    private BroadcastReceiver stopServiceReceiver;
    private AppChecker appChecker;

    public static void start(Context context) {
        context.startService(new Intent(context, ForegroundService.class));

    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ForegroundService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceivers();
        startChecker();
        try {
            createStickyNotification();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter("intentKey"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopChecker();
        removeNotification();
        unregisterReceivers();
        stopSelf();
    }

    private void startChecker() {
        appChecker = new AppChecker();
        appChecker
                .when(getPackageName(), packageName -> {
                    //Toast.makeText(getBaseContext(), "Our app is in the foreground.", Toast.LENGTH_SHORT).show();
                })
                .whenOther(packageName -> {
                    //Toast.makeText(getBaseContext(), "Foreground: " + packageName, Toast.LENGTH_SHORT).show();
                    createStickyNotification();
                })
                .timeout(1000)
                .start(this);
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            curfreq = intent.getStringExtra("curfreq");
            maxfreq = intent.getStringExtra("maxfreq");
            minfreq = intent.getStringExtra("minfreq");
        }
    };

    private void stopChecker() {
        appChecker.stop();
    }

    private void registerReceivers() {
        stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };
        registerReceiver(stopServiceReceiver, new IntentFilter(STOP_SERVICE));
    }

    private void unregisterReceivers() {
        unregisterReceiver(stopServiceReceiver);
    }

    private Notification createStickyNotification() throws PackageManager.NameNotFoundException {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            CharSequence channelName = "CPU Frequency Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // Register the channel with the system
            manager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Stop Service")
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT))
                .setWhen(0)
                .build();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Foreground Application: "+appChecker.getForegroundApp(this))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Your Current Frequency is: "+curfreq +
                            "\nYour Current Maximum Frequency is: "+maxfreq +
                            "\nYour Current Minimum Frequency is: "+minfreq))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);
                   // .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT));
            startForeground(1,mBuilder.build());

        }
        manager.notify(NOTIFICATION_ID, notification);

        return notification;

    }

    private void removeNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        manager.cancel(NOTIFICATION_ID);
    }

}
