package com.cangjie.scalage.kit

import android.view.View

internal class ViewClickWrapper(private val mToast: CJToast, view: View, private val mListener: OnClickListener<Any>) : View.OnClickListener {
    override fun onClick(v: View) {
        mListener.onClick(mToast, v)
    }
    init {
        view.setOnClickListener(this)
    }
}