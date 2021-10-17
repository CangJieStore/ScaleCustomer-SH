package com.cangjie.scalage.core.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.cangjie.scalage.core.widget.CenterDrawableHelper

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/7 11:17
 */
open class CenterDrawableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas?) {
        CenterDrawableHelper.preDraw(this, canvas)
        super.onDraw(canvas)
    }
}