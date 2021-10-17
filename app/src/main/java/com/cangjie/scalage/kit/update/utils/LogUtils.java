package com.cangjie.scalage.kit.update.utils;

import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    /**
     * Log日志工具
     *
     * @param msg
     */
    public static void log(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            if (AppUpdateUtils.getInstance().getUpdateConfig().isDebug()) {
                Log.e("【AppUpdateUtils】", msg);
            }
        }
    }
}
