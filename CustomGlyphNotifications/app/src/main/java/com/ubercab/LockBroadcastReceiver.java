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
        GlyphAnimator.GetInstance().TriggerBreathingAnim();
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
