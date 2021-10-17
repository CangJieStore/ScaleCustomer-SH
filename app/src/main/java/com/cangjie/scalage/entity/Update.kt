package com.cangjie.scalage.entity

data class Update(
    val versionCode: Int,
    val versionName: String,
    val forceUpdate: Boolean,
    val updateLog: String,
    val apkUrl: String
)
