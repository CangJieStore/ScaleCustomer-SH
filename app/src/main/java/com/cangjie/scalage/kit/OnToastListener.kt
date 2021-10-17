package com.cangjie.scalage.kit


interface OnToastListener {
    /**
     * 显示回调
     */
    fun onShow(toast: CJToast?)

    /**
     * 消失回调
     */
    fun onDismiss(toast: CJToast?)
}