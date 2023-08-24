package com.ubercab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class LockBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "LockBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Cancel any existing animations. If there is an ongoing animation, the new
        // animation won't play
        GlyphAnimator.GetInstance().ResetRunningAnim();

        GlyphAnimator.GetInstance().TriggerBreathingAnim();

        // End the animation early, at the end of a breathing cycle.
        // Otherwise the animation will continue infinitely
        TriggerBreathingAnimEnd();
    }

    private void TriggerBreathingAnimEnd() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlyphAnimator.GetInstance().ResetRunningAnim();
            }
        }, 4000);
    }
}
