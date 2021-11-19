package com.cangjie.scalage.ui

/**
 * @author CangJie
 * @date 2021/11/19 09:21
 */
interface ProgressCallback {
    fun progress(pb: Int, status: Int)
}