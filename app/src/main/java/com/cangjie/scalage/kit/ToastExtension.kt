package com.cangjie.scalage.kit

import android.R
import android.app.Activity
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import com.cangjie.scalage.kit.lib.ToastUtils

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/16 10:38
 */

fun show(activity: Activity, @LayoutRes layoutId: Int, @AnimRes aniId: Int, duration: Int) {
    CJToast(activity)
        .setDuration(duration)
        .setView(layoutId)
        .setAnimStyle(aniId)
        .show()
}

fun showWithListener(
    activity: Activity,
    @LayoutRes layoutId: Int,
    @AnimRes aniId: Int,
    duration: Int,
    listener: OnToastListener
) {
    CJToast(activity)
        .setDuration(duration)
        .setView(layoutId)
        .setAnimStyle(aniId)
        .setOnToastListener(listener)
        .show()
}

fun show(activity: Activity, duration: Int, message: String) {
    CJToast(activity)
        .setDuration(duration)
        .setView(ToastUtils.getStyle().createView(activity))
        .setAnimStyle(R.style.Animation_Translucent)
        .setText(R.id.message, message)
        .show()
}
