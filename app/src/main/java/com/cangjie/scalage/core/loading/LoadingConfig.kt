package com.cangjie.scalage.core.loading

import android.graphics.Color
import androidx.annotation.StyleRes

/**
 * @author gumi at 2019-02-25 13:47
 */
class LoadingConfig private constructor() {
    /**
     * 点击外部可以取消
     */
    @JvmField
    var canceledOnTouchOutside = false

    /**
     * 是否可以返回键关闭
     */
    @JvmField
    var cancelable = false

    /**
     * 窗体背景色
     */
    @JvmField
    var backgroundWindowColor = Color.TRANSPARENT

    /**
     * View背景色
     */
    @JvmField
    var backgroundViewColor = Color.parseColor("#b2000000")

    /**
     * View边框的颜色
     */
    @JvmField
    var strokeColor = Color.TRANSPARENT

    /**
     * View背景圆角
     */
    @JvmField
    var cornerRadius = 8f

    /**
     * View边框的宽度
     */
    @JvmField
    var strokeWidth = 0f

    /**
     * Progress的颜色
     */
    var progressColor = Color.WHITE

    /**
     * Progress的宽度
     */
    var progressWidth = 2f

    /**
     * progress背景环的颜色
     */
    var progressRimColor = Color.TRANSPARENT

    /**
     * progress背景环的宽度
     */
    var progressRimWidth = 0

    /**
     * 文字的颜色
     */
    @JvmField
    var textColor = Color.WHITE

    /**
     * 文字的大小:默认12sp
     */
    @JvmField
    var textSize = 12f

    /**
     * 消失的监听
     */
    @JvmField
    var onDialogDismissListener: OnDialogDismissListener? = null

    /**
     * Dialog进出动画
     */
    @JvmField
    var animationID = 0

    /**
     * 布局的Padding--int left, int top, int right, int bottom
     */
    @JvmField
    var paddingLeft = 12f
    @JvmField
    var paddingTop = 12f
    @JvmField
    var paddingRight = 12f
    @JvmField
    var paddingBottom = 12f
    var loadindDrawable = 0

    /**
     * StatusDialog专用：中间图片宽高
     */
    var imgWidth = 40
    var imgHeight = 40

    class Builder {
        private val mDialogConfig: LoadingConfig
        fun build(): LoadingConfig {
            return mDialogConfig
        }

        /**
         * 设置点击外部取消Dialog
         *
         * @param canceledOnTouchOutside
         * @return
         */
        fun isCanceledOnTouchOutside(canceledOnTouchOutside: Boolean): Builder {
            mDialogConfig.canceledOnTouchOutside = canceledOnTouchOutside
            return this
        }

        fun setLoadingDrawable(drawable: Int): Builder {
            mDialogConfig.loadindDrawable = drawable
            return this
        }

        /**
         * 返回键取消
         *
         * @param cancelable
         * @return
         */
        fun isCancelable(cancelable: Boolean): Builder {
            mDialogConfig.cancelable = cancelable
            return this
        }

        fun setBackgroundWindowColor(backgroundWindowColor: Int): Builder {
            mDialogConfig.backgroundWindowColor = backgroundWindowColor
            return this
        }

        fun setBackgroundViewColor(backgroundViewColor: Int): Builder {
            mDialogConfig.backgroundViewColor = backgroundViewColor
            return this
        }

        fun setStrokeColor(strokeColor: Int): Builder {
            mDialogConfig.strokeColor = strokeColor
            return this
        }

        fun setStrokeWidth(strokeWidth: Float): Builder {
            mDialogConfig.strokeWidth = strokeWidth
            return this
        }

        fun setCornerRadius(cornerRadius: Float): Builder {
            mDialogConfig.cornerRadius = cornerRadius
            return this
        }

        fun setProgressColor(progressColor: Int): Builder {
            mDialogConfig.progressColor = progressColor
            return this
        }

        fun setProgressWidth(progressWidth: Float): Builder {
            mDialogConfig.progressWidth = progressWidth
            return this
        }

        fun setProgressRimColor(progressRimColor: Int): Builder {
            mDialogConfig.progressRimColor = progressRimColor
            return this
        }

        fun setProgressRimWidth(progressRimWidth: Int): Builder {
            mDialogConfig.progressRimWidth = progressRimWidth
            return this
        }

        fun setTextColor(textColor: Int): Builder {
            mDialogConfig.textColor = textColor
            return this
        }

        fun setTextSize(textSize: Float): Builder {
            mDialogConfig.textSize = textSize
            return this
        }

        fun setOnDialogDismissListener(onDialogDismissListener: OnDialogDismissListener?): Builder {
            mDialogConfig.onDialogDismissListener = onDialogDismissListener
            return this
        }

        fun setAnimationID(@StyleRes resId: Int): Builder {
            mDialogConfig.animationID = resId
            return this
        }

        fun setImgWidthAndHeight(
            imgWidth: Int,
            imgHeight: Int
        ): Builder {
            mDialogConfig.imgWidth = imgWidth
            mDialogConfig.imgHeight = imgHeight
            return this
        }

        fun setPadding(
            paddingLeft: Int,
            paddingTop: Int,
            paddingRight: Int,
            paddingBottom: Int
        ): Builder {
            mDialogConfig.paddingLeft = paddingLeft.toFloat()
            mDialogConfig.paddingTop = paddingTop.toFloat()
            mDialogConfig.paddingRight = paddingRight.toFloat()
            mDialogConfig.paddingBottom = paddingBottom.toFloat()
            return this
        }

        init {
            mDialogConfig = LoadingConfig()
        }
    }
}