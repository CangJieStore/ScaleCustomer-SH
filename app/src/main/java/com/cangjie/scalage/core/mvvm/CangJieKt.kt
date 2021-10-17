package com.cangjie.scalage.core.mvvm

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/7 22:11
 */
object CangJieKt {

    private lateinit var mConfig: GlobalConfig

    fun install(config: GlobalConfig) {
        mConfig = config
    }

    fun getConfig() = mConfig
}