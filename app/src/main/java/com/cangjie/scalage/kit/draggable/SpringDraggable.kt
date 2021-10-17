package com.cangjie.scalage.kit.draggable

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import com.cangjie.scalage.kit.draggable.BaseDraggable

class SpringDraggable : BaseDraggable() {
    /** 手指按下的坐标  */
    private var mViewDownX = 0f
    private var mViewDownY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val rawMoveX: Int
        val rawMoveY: Int
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下的位置（相对 View 的坐标）
                mViewDownX = event.x
                mViewDownY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                // 记录移动的位置（相对屏幕的坐标）
                rawMoveX = event.rawX.toInt()
                rawMoveY = (event.rawY - statusBarHeight).toInt()
                // 更新移动的位置
                updateLocation(rawMoveX - mViewDownX, rawMoveY - mViewDownY)
            }
            MotionEvent.ACTION_UP -> {
                // 记录移动的位置（相对屏幕的坐标）
                rawMoveX = event.rawX.toInt()
                rawMoveY = (event.rawY - statusBarHeight).toInt()
                // 获取当前屏幕的宽度
                val screenWidth = screenWidth
                // 自动回弹吸附
                val rawFinalX: Float
                rawFinalX = if (rawMoveX < screenWidth / 2) {
                    // 回弹到屏幕左边
                    0f
                } else {
                    // 回弹到屏幕右边
                    screenWidth.toFloat()
                }
                // 从移动的点回弹到边界上
                startAnimation(rawMoveX - mViewDownX, rawFinalX - mViewDownX, rawMoveY - mViewDownY)
                // 如果产生了移动就拦截这个事件（与按下的坐标不一致时）
                return mViewDownX != event.x || mViewDownY != event.y
            }
            else -> {
            }
        }
        return false
    }

    /**
     * 获取屏幕的宽度
     */
    private val screenWidth: Int
        private get() {
            val manager = windowManager!!
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.widthPixels
        }

    /**
     * 执行动画
     *
     * @param startX        X轴起点坐标
     * @param endX          X轴终点坐标
     * @param y             Y轴坐标
     */
    private fun startAnimation(startX: Float, endX: Float, y: Float) {
        val animator = ValueAnimator.ofFloat(startX, endX)
        animator.duration = 500
        animator.addUpdateListener { animation -> updateLocation(animation.animatedValue as Float, y) }
        animator.start()
    }
}