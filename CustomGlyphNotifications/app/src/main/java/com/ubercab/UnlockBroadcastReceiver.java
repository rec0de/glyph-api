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
        GlyphAnimator.GetInstance().TriggerFlashAnim();
        TriggerCycleAnimEnd();
    }

    private void TriggerCycleAnimEnd() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlyphAnimator.GetInstance().ResetRunningAnim();
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
