package com.ubercab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Handler;

import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

public class MusicBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MusicBroadcastReceiver";

    private static MusicBroadcastReceiver instance;

    private Timer progressAnimTimer;
    private final long timerRunIntervalMS = 2000;
    private final int timeDivider = 1000;
    private boolean isTimerPaused;

    private int curTrackProgressSecs;
    private int trackLengthSecs;

    private Handler playbackEventDebounceHandler;
    private Handler playbackProgressHandler;
    private final int debounceMS = 100;
    private boolean wasMusicPreviouslyPaused = true;

    private Runnable playCallback;
    private Runnable pausecallback;

    public MusicBroadcastReceiver(Runnable playCallback, Runnable pauseCallback) {
        this.playCallback = playCallback;
        this.pausecallback = pauseCallback;
        progressAnimTimer = null;
        playbackEventDebounceHandler = new Handler(Looper.getMainLooper());
        playbackProgressHandler = new Handler(Looper.getMainLooper());
    }

    public static void Register(Context context, Runnable playCallback, Runnable pauseCallback) {
        instance = new MusicBroadcastReceiver(playCallback, pauseCallback);
        ContextCompat.registerReceiver(context, instance, CreateIntentFilter(),
                ContextCompat.RECEIVER_EXPORTED);
    }

    public static void Unregister(Context context) {
        context.unregisterReceiver(instance);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String curAction = intent.getAction();

        switch (curAction) {

            // This means that a new track is being played
            case "com.spotify.music.metadatachanged":
                // Gets the total track length in MS
                trackLengthSecs = intent.getIntExtra("length", 0) / timeDivider;
                wasMusicPreviouslyPaused = true;
                break;

            // This occurs when the user presses play or pause, or changes their position in the track
            case "com.spotify.music.playbackstatechanged":
                // Debounce the playback state changed events by cancelling any pending notifications or
                // state handling if we are getting repeated playback state intents
                // in quick succession. Spotify sends multiple of these over however it feels and I
                // wasn't able to figure out a method to their madness.
                playbackProgressHandler.removeCallbacksAndMessages(null); // Cancel any pending progress notifs
                playbackEventDebounceHandler.removeCallbacksAndMessages(null);
                playbackEventDebounceHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HandlePlaybackState(intent);
                    }
                }, debounceMS);
        }
    }


    private void HandlePlaybackState(Intent intent) {
        boolean isPlaying = intent.getBooleanExtra("playing", false);
        int latestTrackProgressSecs = intent.getIntExtra("playbackPosition", 0) / timeDivider;
        latestTrackProgressSecs = trackLengthSecs - latestTrackProgressSecs;

        if (!isPlaying) {
            pausecallback.run();
            wasMusicPreviouslyPaused = true;
            PauseProgressAnimTimer();
            return;
        }

        // wasMusicPreviouslyPaused tracks whether we are playing from a previously paused state.
        // We could just check for isPlaying instead and that would work. However we also want
        // the below initialization to take place when we skip to a new song. This is why
        // metadataChanged sets wasMusicPreviouslyPaused to true
        // OR
        // Check if we scrubbed backwards through a song, in this case we need to InitializeProgressAnim again
        // The condition is > not less than (<) because the progress that needs to be sent to the GlyphService
        // walks backwards
        if (wasMusicPreviouslyPaused || latestTrackProgressSecs > curTrackProgressSecs) {
            playCallback.run();
            playbackProgressHandler.removeCallbacksAndMessages(null); // Cancel any pending progress notifs
            GlyphAnimator.GetInstance().InitializeProgressAnim(trackLengthSecs);
            wasMusicPreviouslyPaused = false;
        }

        // Need to run this after a delay because the GlyphService can't respond to notifications too quickly
        final int finalLatestTrackProgressSecs = latestTrackProgressSecs;
        playbackProgressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                StartProgressAnimTimer(finalLatestTrackProgressSecs);
            }
        }, 300);
    }


    private void StartProgressAnimTimer(int startTrackProgressSecs) {
        if (progressAnimTimer != null) {
            progressAnimTimer.cancel();
        }

        progressAnimTimer = new Timer();
        curTrackProgressSecs = startTrackProgressSecs;
        isTimerPaused = false;

        progressAnimTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isTimerPaused) {
                    return;
                }
//                Log.d(TAG, "Updating progress to: " + curTrackProgressSecs);
                GlyphAnimator.GetInstance().UpdateProgressAnim(curTrackProgressSecs);
                curTrackProgressSecs -= timerRunIntervalMS / 1000;
            }
        }, 0, timerRunIntervalMS);
    }

    private void PauseProgressAnimTimer() {
        if (progressAnimTimer == null) {
            return;
        }
        isTimerPaused = true;
        GlyphAnimator.GetInstance().TurnOffRunningAnim();
    }

    private static IntentFilter CreateIntentFilter() {
        IntentFilter iF = new IntentFilter();
        // Need to enable Broadcast Device Status in Spotify for this to work
        // Spotify implementation is detailed here:
        // https://developer.spotify.com/documentation/android/tutorials/android-media-notifications
        iF.addAction("com.spotify.music.metadatachanged");
        iF.addAction("com.spotify.music.playbackstatechanged");
        return iF;
    }
}
