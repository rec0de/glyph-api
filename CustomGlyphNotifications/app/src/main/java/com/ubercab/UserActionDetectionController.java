package com.ubercab;

import android.content.Context;
import android.util.Log;

public class UserActionDetectionController {

    private final String TAG = "UserActionDetectionController";

    private Context context;

    public UserActionDetectionController(Context context) {
        this.context = context;
        RegisterUserActionBroadcastReceivers();
    }

    private void RegisterUserActionBroadcastReceivers() {
        UnlockBroadcastReceiver.Register(context);
        LockBroadcastReceiver.Register(context);
        MusicBroadcastReceiver.Register(
            context,
            () -> {
                MusicBroadcastPlayCallback();
            },
            () -> {
                MusicBroadcastPauseCallback();
            }
        );
    }

    private void MusicBroadcastPlayCallback() {
        Log.d(TAG, "Disabling lock/unlock anims");
        UnlockBroadcastReceiver.Unregister(context);
        LockBroadcastReceiver.Unregister(context);
    }

    private void MusicBroadcastPauseCallback() {
        Log.d(TAG, "Enabling lock/unlock anims");
        UnlockBroadcastReceiver.Register(context);
        LockBroadcastReceiver.Register(context);
    }
}
