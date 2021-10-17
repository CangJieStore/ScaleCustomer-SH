package com.cangjie.scalage.core.loading

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import com.cangjie.scalage.R

/**
 * @author gumi at 2019-03-02 09:49
 */
class LoadingIndicator : View {
    private var mMinWidth = 0
    private var mMaxWidth = 0
    private var mMinHeight = 0
    private var mMaxHeight = 0
    private var indicatorName: String? = null
    private var indicatorColor = 0
    private var indicatorSpeed = 0
    private var mIndicator: IndicatorDrawable? = null

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context, attrs, 0, R.style.LoadingIndicator)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, R.style.LoadingIndicator)
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val ta = context.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingIndicator,
            defStyleAttr,
            defStyleRes
        )
        mMinWidth = ta.getDimensionPixelSize(R.styleable.LoadingIndicator_minWidth, 60)
        mMaxWidth = ta.getDimensionPixelSize(R.styleable.LoadingIndicator_maxWidth, 60)
        mMinHeight = ta.getDimensionPixelSize(R.styleable.LoadingIndicator_minHeight, 60)
        mMaxHeight = ta.getDimensionPixelSize(R.styleable.LoadingIndicator_maxHeight, 60)
        indicatorName = ta.getString(R.styleable.LoadingIndicator_indicatorName)
        indicatorColor =
            ta.getColor(R.styleable.LoadingIndicator_indicatorColor, Color.WHITE)
        indicatorSpeed = ta.getInteger(R.styleable.LoadingIndicator_indicatorSpeed, 0)
        ta.recycle()
        val indicatorDrawable = getIndicator(indicatorName, context)
        indicatorDrawable!!.callback = this
        mIndicator = indicatorDrawable
    }

    fun getIndicator(
        indicatorName: String?,
        context: Context?
    ): IndicatorDrawable? {
        return if (TextUtils.isEmpty(indicatorName)) {
            null
        } else ArcRotateIndicator(context, indicatorColor, indicatorSpeed)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === mIndicator || super.verifyDrawable(who)
    }

    override fun invalidateDrawable(dr: Drawable) {
        if (verifyDrawable(dr)) {
            val dirty = dr.bounds
            val scrollX = scrollX + paddingLeft
            val scrollY = scrollY + paddingTop
            invalidate(
                dirty.left + scrollX, dirty.top + scrollY,
                dirty.right + scrollX, dirty.bottom + scrollY
            )
        } else {
            super.invalidateDrawable(dr)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.save()
        mIndicator!!.draw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateDrawableBounds(w, h)
    }

    private fun updateDrawableBounds(w: Int, h: Int) {
        var w = w
        var h = h
        w -= (paddingRight + paddingLeft)
        h -= (paddingTop + paddingBottom)
        var left = 0
        var top = 0
        var right = w
        var bottom = h
        if (mIndicator != null) {
            val intrinsicWidth = mIndicator!!.intrinsicWidth
            val intrinsicHeight = mIndicator!!.intrinsicHeight
            val intrinsicAspect = intrinsicWidth.toFloat() / intrinsicHeight
            val boundAspect = w.toFloat() / h
            if (intrinsicAspect != boundAspect) {
                if (boundAspect > intrinsicAspect) {
                    // New width is larger. Make it smaller to match height.
                    val width = (h * intrinsicAspect).toInt()
                    left = (w - width) / 2
                    right = left + width
                } else {
                    // New height is larger. Make it smaller to match width.
                    val height = (w * (1 / intrinsicAspect)).toInt()
                    top = (h - height) / 2
                    bottom = top + height
                }
            }
            mIndicator!!.setBounds(left, top, right, bottom)
        }
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var dw = 0
        var dh = 0
        val d: Drawable? = mIndicator
        if (d != null) {
            dw = mMinWidth.coerceAtLeast(mMaxWidth.coerceAtMost(d.intrinsicWidth))
            dh = mMinHeight.coerceAtLeast(mMaxHeight.coerceAtMost(d.intrinsicHeight))
        }
        dw += (paddingLeft + paddingRight)
        dh += (paddingTop + paddingBottom)
        val measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0)
        val measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    private fun startAnimation() {
        if (visibility != VISIBLE) {
            return
        }
        mIndicator!!.start()
        postInvalidate()
    }

    private fun stopAnimation() {
        mIndicator!!.stop()
        postInvalidate()
    }
}