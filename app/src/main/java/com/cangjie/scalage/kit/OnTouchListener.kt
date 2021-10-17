package com.cangjie.scalage.kit

import android.view.MotionEvent

interface OnTouchListener<V : Any> {
    /**
     * 触摸回调
     */
    fun onTouch(toast: CJToast?, view: V, event: MotionEvent?): Boolean
}