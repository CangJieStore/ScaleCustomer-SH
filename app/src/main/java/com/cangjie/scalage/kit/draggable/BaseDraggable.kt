package com.cangjie.scalage.kit.draggable

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.cangjie.scalage.kit.CJToast

abstract class BaseDraggable : View.OnTouchListener {
    protected var xToast: CJToast? = null
        private set
    protected var rootView: View? = null
        private set
    protected var windowManager: WindowManager? = null
        private set
    protected var windowParams: WindowManager.LayoutParams? = null
        private set

    /**
     * Toast 显示后回调这个类
     */
    fun start(toast: CJToast) {
        xToast = toast
        rootView = toast.view
        windowManager = toast.windowManager
        windowParams = toast.windowParams
        rootView!!.setOnTouchListener(this)
    }

    /**
     * 获取状态栏的高度
     */
    protected val statusBarHeight: Int
        protected get() {
            val frame = Rect()
            rootView!!.getWindowVisibleDisplayFrame(frame)
            return frame.top
        }

    protected fun updateLocation(x: Float, y: Float) {
        updateLocation(x.toInt(), y.toInt())
    }

    /**
     * 更新 WindowManager 所在的位置
     */
    protected fun updateLocation(x: Int, y: Int) {
        if (windowParams!!.x != x || windowParams!!.y != y) {
            windowParams!!.x = x
            windowParams!!.y = y
            // 一定要先设置重心位置为左上角
            windowParams!!.gravity = Gravity.TOP or Gravity.START
            try {
                windowManager!!.updateViewLayout(rootView, windowParams)
            } catch (ignored: IllegalArgumentException) {
                // 当 WindowManager 已经消失时调用会发生崩溃
                // IllegalArgumentException: View not attached to window manager
            }
        }
    }
}