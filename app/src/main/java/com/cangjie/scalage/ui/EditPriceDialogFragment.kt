package com.cangjie.scalage.ui

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.cangjie.frame.kit.bottom.BaseBottomDialog
import com.cangjie.frame.kit.keyboard.KeyboardUtil
import com.cangjie.frame.kit.keyboard.NumberKeyboardView
import com.cangjie.scalage.R
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.destroyImmersionBar
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author: guruohan
 * @date: 2021/11/6
 */
class EditPriceDialogFragment(title: String, hint: String) : BaseBottomDialog() {

    override val layoutRes: Int = R.layout.dialog_edit_price
    private var contentCallback: ContentCallback? = null

    private var mTitle = ""
    private var mHint = ""

    init {
        this.mHint = hint
        this.mTitle = title
    }

    override fun onStart() {
        super.onStart()
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            init()
        }
    }

    override fun bindView(v: View?) {
        v?.let {
            it.findViewById<AppCompatTextView>(R.id.txt_dialog_title).text = mTitle
            val content = it.findViewById<AppCompatEditText>(R.id.edit_reply_commit)
            content.hint = mHint
            val keyboardView = it.findViewById<NumberKeyboardView>(R.id.keyboard_view);
            val keyboardUtil = KeyboardUtil(requireActivity(), keyboardView)
            keyboardUtil.attachTo(content)
            keyboardUtil.setOnOkClick {
                val editContent = content.text.toString().trim()
                contentCallback?.content(editContent)
                dismiss()
            }
            keyboardUtil.setOnCancelClick {
                dismiss()
            }
        }
    }

    interface ContentCallback {
        fun content(content: String?)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.let { destroyImmersionBar(it) }
    }

    fun setContentCallback(cb: ContentCallback?): EditPriceDialogFragment {
        contentCallback = cb
        return this
    }
}