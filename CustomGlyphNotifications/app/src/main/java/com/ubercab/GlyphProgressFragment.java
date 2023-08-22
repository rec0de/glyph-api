package com.ubercab;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.ubercab.databinding.FragmentFirstBinding;

public class GlyphProgressFragment extends Fragment {

    private FragmentFirstBinding binding;

    private final String TAG = "GlyphProgressFragment";
    private final String CHANNEL_ID = "GlyphProgressChannel";
    private final int NOTIF_ID = 24;

    private final int MAX_MINS_LEFT = 10;
    private int minsLeft = MAX_MINS_LEFT;
    private boolean isProgressInit = false;

    private NotificationManagerCompat notificationManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CreateNotificationChannel();
        notificationManager = NotificationManagerCompat.from(MainActivity.context);

        binding.buttonAnim.setOnClickListener(view1 -> {
            SendAnimNotification();
        });

        binding.buttonTimeLeftDecrement.setOnClickListener(view1 -> {
            if (isProgressInit) {
                minsLeft--;
            }
            isProgressInit = true;
            SendProgressNotification();
        });

        binding.buttonTimeLeftIncrement.setOnClickListener(view1 -> {
            minsLeft++;
            SendProgressNotification();
        });

        binding.buttonCancel.setOnClickListener(view1 -> {
            SendCancelNotification();
            minsLeft = MAX_MINS_LEFT;
            isProgressInit = false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void SendProgressNotification() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Pickup in " + minsLeft + " mins");

        // Build notification
        Notification.Builder notifBuilder = new Notification.Builder(MainActivity.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addExtras(extras)
                .setAutoCancel(true);

        // Send notification
        notificationManager.notify(NOTIF_ID, notifBuilder.build());
        HideNotification();
    }


    private void SendAnimNotification() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Finding you a driver");

        // Build notification
        Notification.Builder notifBuilder = new Notification.Builder(MainActivity.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addExtras(extras)
                .setAutoCancel(true);

        // Send notification
        notificationManager.notify(NOTIF_ID, notifBuilder.build());
        HideNotification();
    }


    private void SendCancelNotification() {
        Bundle extras = new Bundle();
        extras.putString("android.title", "Cancel Trip");

        // Build notification
        Notification.Builder notifBuilder = new Notification.Builder(MainActivity.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addExtras(extras)
                .setAutoCancel(true);

        // Send notification
        notificationManager.notify(NOTIF_ID, notifBuilder.build());
        HideNotification();
    }


    private void HideNotification() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel(NOTIF_ID);
            }
        }, 200);
    }


    private void CreateNotificationChannel() {
        CharSequence name = "Glyph Progress Channel";
        String description = "Channel used to update Glyph progress";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = MainActivity.context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}