package com.cangjie.scalage.core.utils

import android.content.Context

/**
 * @author G.god
 * Create by AS 2020/5/20 13:42
 */
object DensityUtil {

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}