package com.ubercab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.concurrent.locks.Lock;

public class UserActionDetectionForegroundService extends Service {

    private final String TAG = "UserActionDetectionForegroundService";

    private static final int NOTIF_ID = 1;
    private static final String CHANNEL_ID = "UserActionDetectionForegroundServiceChannel";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        RegisterUserActionBroadcastReceivers();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("User Action Detection Service")
                .setContentText("Actively detecting actions to trigger glyph animations...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(NOTIF_ID, notification);

        return START_STICKY;
    }

    private void createNotificationChannel() {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "User Action Detection Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    private void RegisterUserActionBroadcastReceivers() {
        Context context = getApplicationContext();
        UnlockBroadcastReceiver.Register(context);
        LockBroadcastReceiver.Register(context);
        MusicBroadcastReceiver.Register(context);
    }
}
