package com.example.rishivijaygajelli.appconengine.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import com.example.rishivijaygajelli.appconengine.R;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SleepService extends Service {

    private final static int NOTIFICATION_ID = 2345;
    private final static String STOP_SERVICE = SleepService.class.getPackage() + ".stop";
    NotificationChannel notificationChannel;
    String channelId = "2";
    String curfreq = null;
    String maxfreq = null;
    String minfreq = null;
    private BroadcastReceiver stopServiceReceiver;
    private BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            curfreq = intent.getStringExtra("curfreq");
            maxfreq = intent.getStringExtra("maxfreq");
            minfreq = intent.getStringExtra("minfreq");
        }
    };

    public static void start(Context context) {
        context.startService(new Intent(context, SleepService.class));

    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, SleepService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createStickyNotification();
        registerReceivers();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver2, new IntentFilter("intentKey2"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotification();
        unregisterReceivers();
        stopSelf();
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

    private Notification createStickyNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence channelName = "CPU Sleep Notification";
            int importance = NotificationManager.IMPORTANCE_LOW;
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
                .setWhen(0)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Sleep Service")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("This is a service to record sleep activities of the device"))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true);
            // .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT));
            startForeground(1, mBuilder.build());

        }
        manager.notify(NOTIFICATION_ID, notification);

        return notification;

    }

    private void removeNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        manager.cancel(NOTIFICATION_ID);
    }
}
