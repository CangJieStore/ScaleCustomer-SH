package com.cangjie.scalage.kit.draggable

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.cangjie.scalage.kit.draggable.BaseDraggable

class MovingDraggable : BaseDraggable() {
    /** 手指按下的坐标  */
    private var mViewDownX = 0f
    private var mViewDownY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下的位置（相对 View 的坐标）
                mViewDownX = event.x
                mViewDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                // 记录移动的位置（相对屏幕的坐标）
                val rawMoveX = event.rawX
                val rawMoveY = event.rawY - statusBarHeight
                // 更新移动的位置
                updateLocation(rawMoveX - mViewDownX, rawMoveY - mViewDownY)
            }
            MotionEvent.ACTION_UP ->                 // 如果产生了移动就拦截这个事件（与按下的坐标不一致时）
                return mViewDownX != event.x || mViewDownY != event.y
            else -> {
            }
        }
        return false
    }
}