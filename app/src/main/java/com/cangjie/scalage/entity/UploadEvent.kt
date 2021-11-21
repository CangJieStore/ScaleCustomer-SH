package com.cangjie.scalage.entity

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/16 15:07
 */
data class UploadEvent(
    val code: Int,
    val waitSize: Int,
    val totalSize: Int
)