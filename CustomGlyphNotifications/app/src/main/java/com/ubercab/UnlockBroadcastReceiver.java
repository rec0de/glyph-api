package com.ubercab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class UnlockBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "UnlockBroadcastReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        // Cancel any existing animations. If there is an ongoing animation, the new
        // animation won't play
        GlyphAnimator.GetInstance().ResetRunningAnim();

        GlyphAnimator.GetInstance().TriggerFlashAnim();

        // End the animation early. Otherwise the animation continues infinitely
        TriggerCycleAnimEnd();
    }


    private void TriggerCycleAnimEnd() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlyphAnimator.GetInstance().ResetRunningAnim();

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
                GlyphAnimator.GetInstance().ResetRunningAnim();
            }
        }, 500);
    }
}
