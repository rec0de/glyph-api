package com.ubercab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.ContextCompat;

public class UnlockBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "UnlockBroadcastReceiver";

    private static UnlockBroadcastReceiver instance;

    public static void Register(Context context) {
        instance = new UnlockBroadcastReceiver();
        IntentFilter filter = new IntentFilter("android.intent.action.USER_PRESENT");
        ContextCompat.registerReceiver(context, instance, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Cancel any existing animations. If there is an ongoing animation, the new
        // animation won't play
        GlyphAnimator.GetInstance().TurnOffRunningAnim();

        GlyphAnimator.GetInstance().TriggerFlashAnim();

        // End the animation early. Otherwise the animation continues infinitely
        TriggerCycleAnimEnd();
    }


    private void TriggerCycleAnimEnd() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlyphAnimator.GetInstance().TurnOffRunningAnim();

                // For the flash/cycle animation, sometimes the final LED remains stuck.
                // The below method resets any running animations again to fix this.
                HandleStuckLEDOnCycleEnd();
            }
        }, 200);
    }


    private void HandleStuckLEDOnCycleEnd() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlyphAnimator.GetInstance().TurnOffRunningAnim();
            }
        }, 500);
    }
}
