package com.ubercab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class GlyphAnimator {

    private static GlyphAnimator instance;
    public static GlyphAnimator GetInstance() {
        if (instance == null) {
            instance = new GlyphAnimator();
        }
        return instance;
    }

    private final String TAG = "GlyphAnimator";

    private final String CHANNEL_ID = "GlyphAnimatorChannel";
    private final int NOTIF_ID = 24;

    private NotificationManager notificationManager;

    private int maxProgressAmt;
    private boolean isProgressAnimInt;

    public GlyphAnimator() {
        CreateNotificationChannel();
        TurnOffRunningAnim();
        ResetProgressAnimState();
    }

    public void TriggerBreathingAnim() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Finding you a driver");
        SendSpoofNotification(extras);
    }

    public void TriggerFlashAnim() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Your driver is here");
        SendSpoofNotification(extras);
    }


    public void InitializeProgressAnim(int maxProgressAmt) {
        isProgressAnimInt = true;
        TurnOffRunningAnim();
        this.maxProgressAmt = maxProgressAmt;
        UpdateProgressAnim(this.maxProgressAmt);
    }


    public void UpdateProgressAnim(int newProgressAmt) {
        if (!isProgressAnimInt) {
            Log.w(TAG, "Initialize progress anim before updating progress");
            return;
        }
        Bundle extras = new Bundle();
        extras.putString("android.title", "Pickup in " + newProgressAmt + " mins");
        SendSpoofNotification(extras);
    }


    public void ResetProgressAnimState() {
        isProgressAnimInt = false;
        maxProgressAmt = 0;
    }


    public void TurnOffRunningAnim() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Cancel Trip");
        SendSpoofNotification(extras);
    }


    private void SendSpoofNotification(Bundle extras) {
        // Build notification
        Notification.Builder notifBuilder = new Notification.Builder(MainActivity.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addExtras(extras)
                .setAutoCancel(true);

        // Send notification
        notificationManager.notify(NOTIF_ID, notifBuilder.build());
        HideRunningSpoofNotification();
    }


    private void HideRunningSpoofNotification() {
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                notificationManager.cancel(NOTIF_ID);
//            }
//        }, 500);
    }


    private void CreateNotificationChannel() {
        CharSequence name = "Glyph Animator Channel";
        String description = "Channel used to perform custom control of Glyph Interface";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setImportance(importance);

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager = MainActivity.context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
