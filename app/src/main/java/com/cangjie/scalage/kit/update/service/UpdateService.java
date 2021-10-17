package com.cangjie.scalage.kit.update.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class UpdateService extends Service {

    private UpdateReceiver updateReceiver = new UpdateReceiver();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(updateReceiver, new IntentFilter(getPackageName() + UpdateReceiver.DOWNLOAD_ONLY));
        registerReceiver(updateReceiver, new IntentFilter(getPackageName() + UpdateReceiver.RE_DOWNLOAD));
        registerReceiver(updateReceiver, new IntentFilter(getPackageName() + UpdateReceiver.CANCEL_DOWNLOAD));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
    }
}
