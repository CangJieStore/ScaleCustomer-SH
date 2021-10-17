package com.cangjie.scalage.kit

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatTextView
import java.io.File

class DigitalText(
    context: Context,
    @Nullable attrs: AttributeSet?
) : AppCompatTextView(context, attrs) {
    private fun init(context: Context) {
        val path = "fonts" + File.separator + "digital-7.ttf"
        val am = context.assets
        val tf = Typeface.createFromAsset(am, path)
        typeface = tf
    }

    init {
        init(context)
    }
}