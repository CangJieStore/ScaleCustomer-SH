package com.cangjie.scalage.core.loading

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.cangjie.scalage.core.utils.DensityUtil.dip2px
import com.cangjie.scalage.R

/**
 * @author gumi at 2019-02-25 13:01
 */
object LoadingDialog {
    private const val LoadingDefaultMsg = "加载中"
    private var mDialog: Dialog? = null
    private var mDialogConfig: LoadingConfig? = null
    private var dialog_window_background: RelativeLayout? = null
    private var dialog_view_bg: RelativeLayout? = null
    private var tv_show: TextView? = null
    private var progress: LoadingIndicator? = null
    private fun initDialog(mContext: Context) {
        val inflater = LayoutInflater.from(mContext)
        val mProgressDialogView =
            inflater.inflate(R.layout.loading_process, null)
        mDialog = Dialog(mContext, R.style.LoadingDialog)
        mDialog!!.setCancelable(false)
        mDialog!!.setCanceledOnTouchOutside(false)
        mDialog!!.setContentView(mProgressDialogView)

        //设置整个Dialog的宽高
        val resources = mContext.resources
        val dm = resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels
        val layoutParams =
            mDialog!!.window!!.attributes
        layoutParams.width = screenW
        layoutParams.height = screenH
        mDialog!!.window!!.attributes = layoutParams

        //布局相关
        dialog_window_background =
            mProgressDialogView.findViewById(R.id.bg_window)
        dialog_view_bg =
            mProgressDialogView.findViewById(R.id.dialog_view_bg)
        tv_show = mProgressDialogView.findViewById(R.id.tv_show)
        progress = mProgressDialogView.findViewById(R.id.progress_wheel)
        configView(mContext)
    }

    private fun configView(mContext: Context) {
        if (mDialogConfig == null) {
            mDialogConfig = LoadingConfig.Builder().build()
        }
        //设置动画
        if (mDialogConfig!!.animationID != 0 && mDialog!!.window != null) {
            try {
                mDialog!!.window!!
                    .setWindowAnimations(mDialogConfig!!.animationID)
            } catch (e: Exception) {
            }
        }
        //点击外部可以取消
        mDialog!!.setCanceledOnTouchOutside(mDialogConfig!!.canceledOnTouchOutside)
        //返回键取消
        mDialog!!.setCancelable(mDialogConfig!!.cancelable)
        //window背景色
        dialog_window_background!!.setBackgroundColor(mDialogConfig!!.backgroundWindowColor)
        //弹框背景
        val myGrad = GradientDrawable()
        myGrad.setColor(mDialogConfig!!.backgroundViewColor)
        myGrad.setStroke(
            dip2px(
                mContext,
                mDialogConfig!!.strokeWidth
            ), mDialogConfig!!.strokeColor
        )
        myGrad.cornerRadius = dip2px(
            mContext,
            mDialogConfig!!.cornerRadius
        ).toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog_view_bg!!.background = myGrad
        } else {
            dialog_view_bg!!.setBackgroundDrawable(myGrad)
        }
        dialog_view_bg!!.setPadding(
            dip2px(
                mContext,
                mDialogConfig!!.paddingLeft
            ),
            dip2px(
                mContext,
                mDialogConfig!!.paddingTop
            ),
            dip2px(
                mContext,
                mDialogConfig!!.paddingRight
            ),
            dip2px(
                mContext,
                mDialogConfig!!.paddingBottom
            )
        )

        //文字颜色设置
        tv_show!!.setTextColor(mDialogConfig!!.textColor)
        tv_show!!.textSize = mDialogConfig!!.textSize

        //点击事件
        dialog_window_background!!.setOnClickListener { v: View? ->
            //取消Dialog
            if (mDialogConfig != null && mDialogConfig!!.canceledOnTouchOutside) {
                dismissProgress()
            }
        }
    }

    fun showProgress(context: Context, mDialogConfig: LoadingConfig?) {
        showProgress(context, LoadingDefaultMsg, mDialogConfig)
    }

    @JvmOverloads
    fun showProgress(
        context: Context,
        msg: String? = LoadingDefaultMsg,
        dialogConfig: LoadingConfig? = null
    ) {
        var dialogConfig = dialogConfig
        try {
            dismissProgress()
            //设置配置
            if (dialogConfig == null) {
                dialogConfig = LoadingConfig.Builder().build()
            }
            mDialogConfig = dialogConfig
            initDialog(context)
            if (mDialog != null && tv_show != null) {
                if (TextUtils.isEmpty(msg)) {
                    tv_show!!.visibility = View.GONE
                } else {
                    tv_show!!.visibility = View.VISIBLE
                    tv_show!!.text = msg
                }
                mDialog!!.show()
            }
        } catch (e: Exception) {
        }
    }

    fun dismissProgress() {
        try {
            if (mDialog != null && mDialog!!.isShowing) {
                //判断是不是有监听
                if (mDialogConfig!!.onDialogDismissListener != null) {
                    mDialogConfig!!.onDialogDismissListener!!.onDismiss()
                    mDialogConfig!!.onDialogDismissListener = null
                }
                mDialogConfig = null
                dialog_window_background = null
                dialog_view_bg = null
                tv_show = null
                mDialog!!.dismiss()
                mDialog = null
            }
        } catch (e: Exception) {
        }
    }

    val isShowing: Boolean
        get() = if (mDialog != null) {
            mDialog!!.isShowing
        } else false
}