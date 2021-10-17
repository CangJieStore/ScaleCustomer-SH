package com.cangjie.scalage.kit.update.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.cangjie.scalage.kit.update.interfaces.AppDownloadListener;
import com.cangjie.scalage.kit.update.model.DownloadInfo;
import com.cangjie.scalage.kit.update.utils.RootActivity;


public class UpdateBackgroundActivity extends RootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.x = 0;
        attributes.y = 0;
        attributes.height = 1;
        attributes.width = 1;

        download();
        finish();
    }

    @Override
    public AppDownloadListener obtainDownloadListener() {
        return null;
    }

    /**
     * 启动Activity
     *
     * @param context
     * @param info
     */
    public static void launch(Context context, DownloadInfo info) {
        launchActivity(context, info, UpdateBackgroundActivity.class);
    }

}
