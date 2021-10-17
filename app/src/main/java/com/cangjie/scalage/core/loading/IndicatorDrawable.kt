package com.cangjie.scalage.core.loading

import android.animation.Animator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Log
import java.util.*

/**
 * @author gumi at 2019-03-02 09:49
 */
abstract class IndicatorDrawable : Drawable(), Animatable {
    private var mAnimatorsList: ArrayList<Animator>? = null
    private val mBounds = Rect()

    @JvmField
    protected var mPaint = Paint()

    @JvmField
    protected var indicatorColor = Color.WHITE

    @JvmField
    protected var indicatorSpeed = 0
    protected abstract fun init(context: Context?)
    protected abstract val animation: ArrayList<Animator>?
    protected abstract fun draw(
        canvas: Canvas?,
        paint: Paint?
    )

    override fun getAlpha(): Int {
        return super.getAlpha()
    }

    override fun setAlpha(alpha: Int) {}
    override fun getColorFilter(): ColorFilter? {
        return super.getColorFilter()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mBounds.set(bounds)
    }

    protected val width: Int
        protected get() = mBounds.width()

    protected val height: Int
        protected get() = mBounds.height()

    override fun draw(canvas: Canvas) {
        draw(canvas, mPaint)
    }

    override fun start() {
        if (mAnimatorsList == null) {
            mAnimatorsList = animation
        }
        if (mAnimatorsList != null) {
            startAnimation()
        }
    }

    override fun stop() {
        if (mAnimatorsList != null) {
            stopAnimation()
        }
    }

    override fun isRunning(): Boolean {
        return false
    }

    private fun startAnimation() {
        for (animator in mAnimatorsList!!) {
            if (!animator.isStarted) {
                animator.start()
            }
        }
    }

    private fun stopAnimation() {
        for (animator in mAnimatorsList!!) {
            if (animator.isStarted) {
                animator.end()
            }
        }
    }

    companion object {
        @JvmStatic
        fun dip2px(context: Context, dpValue: Float): Float {
            val scale = context.resources.displayMetrics.density
            return dpValue * scale + 0.5f
        }
    }
}