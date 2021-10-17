package com.cangjie.scalage.core.loading

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.LinearInterpolator
import java.util.*

/**
 * @author gumi at 2019-03-02 09:54
 */
class ArcRotateIndicator(
    context: Context?,
    indicatorColor: Int,
    indicatorSpeed: Int
) : IndicatorDrawable() {
    private var inRadius = 0f
    private var outRadius = 0f
    private var inRectF: RectF? = null
    private var outRectF: RectF? = null
    private var mAnimatedValue = 0f
    override fun init(context: Context?) {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = dip2px(context!!, 1.0f)
        mPaint.color = indicatorColor
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.isDither = true
        inRadius = dip2px(context, 5.0f)
        outRadius = dip2px(context, 10.0f)
    }

    override val animation: ArrayList<Animator>
        protected get() {
            val list =
                ArrayList<Animator>()
            val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator.addUpdateListener { valueAnimator1: ValueAnimator ->
                mAnimatedValue = valueAnimator1.animatedValue as Float
                invalidateSelf()
            }
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.repeatCount = ValueAnimator.INFINITE
            valueAnimator.duration = indicatorSpeed.toLong()
            list.add(valueAnimator)
            return list
        }

    override fun draw(
        canvas: Canvas?,
        paint: Paint?
    ) {
        if (inRectF == null || outRectF == null) {
            inRectF = RectF()
            outRectF = RectF()
            inRectF!![width / 2 - inRadius, height / 2 - inRadius, width / 2 + inRadius] =
                height / 2 + inRadius
            outRectF!![width / 2 - outRadius, height / 2 - outRadius, width / 2 + outRadius] =
                height / 2 + outRadius
        }
        val rotateAngle = (360 * mAnimatedValue).toInt()
        canvas!!.save()

        //外圆
        canvas.drawArc(
            inRectF!!,
            rotateAngle % 360.toFloat(),
            IN_ANGLE.toFloat(),
            false,
            paint!!
        )
        //内圆
        canvas.drawArc(
            outRectF!!,
            270 - rotateAngle % 360.toFloat(),
            OUT_ANGLE.toFloat(),
            false,
            paint
        )
        canvas.restore()
    }

    companion object {
        private const val IN_ANGLE = 90
        private const val OUT_ANGLE = 270
    }

    init {
        this.indicatorColor = indicatorColor
        this.indicatorSpeed = indicatorSpeed
        if (indicatorSpeed <= 0) {
            this.indicatorSpeed = 2000
        }
        init(context)
    }
}