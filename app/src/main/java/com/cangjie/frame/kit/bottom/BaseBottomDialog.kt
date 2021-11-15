package com.cangjie.frame.kit.bottom

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.cangjie.scalage.R


abstract class BaseBottomDialog : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(cancelOutside)
        val v = inflater.inflate(layoutRes, container, false)
        bindView(v)
        return v
    }

    @get:LayoutRes
    abstract val layoutRes: Int
    abstract fun bindView(v: View?)
    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val params = window!!.attributes
        params.dimAmount = dimAmount
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        if (height > 0) {
            params.height = height
        } else {
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        params.gravity = Gravity.BOTTOM
        window.attributes = params
    }

    open val height: Int
        get() = -1
    open val dimAmount: Float
        get() = 0.2f
    open val cancelOutside: Boolean
        get() = true
    open val fragmentTag: String?
        get() = TAG

    fun show(fragmentManager: FragmentManager?) {
        if (fragmentManager != null) {
            show(fragmentManager, fragmentTag)
        }
    }

    companion object {
        private const val TAG = "base_bottom_dialog"
    }
}