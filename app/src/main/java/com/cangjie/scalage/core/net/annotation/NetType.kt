package com.cangjie.scalage.core.net.annotation

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/14 10:29
 */
@Target(AnnotationTarget.TYPE)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class NetType {
    companion object {
        // wifi
        const val WIFI = "WIFI"

        // 手机网络
        const val NET = "NET"

        // 未识别网络
        const val NET_UNKNOWN = "NET_UNKNOWN"

        // 没有网络
        const val NONE = "NONE"
    }
}