package com.cangjie.scalage.ui

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.KeyboardUtils.showSoftInput
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.SubmitAdapter
import com.cangjie.scalage.databinding.DialogEditShellBinding
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar


/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/15 09:35
 */
class EditShellDialogFragment : DialogFragment() {


    private var editShellBinding: DialogEditShellBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.PreviewDialog)
    }


    override fun onStart() {
        super.onStart()
        dialog!!.setCanceledOnTouchOutside(false)
        val dialogWindow = dialog!!.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        val lp = dialogWindow.attributes
        val displayMetrics = requireContext().resources.displayMetrics
        lp.height = (displayMetrics.heightPixels * 0.32f).toInt()
        lp.width = (displayMetrics.widthPixels * 0.36f).toInt()
        dialogWindow.attributes = lp
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editShellBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_edit_shell, container, false)
        return editShellBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editShellBinding?.let {
            showSoftInput()
            it.actionCancel.setOnClickListener {
                editShellBinding?.editShell?.let { it1 ->
                    toggleSoftKeyboard(
                        requireActivity(),
                        it1, false
                    )
                }
                dismiss()
            }
            it.actionDone.setOnClickListener {
                var shell = editShellBinding?.editShell?.text!!.trim().toString()
                if (shell.isEmpty()) {
                    action?.submit("0.0")
                } else {
                    action?.submit(shell)
                }
                editShellBinding?.editShell?.let { it1 ->
                    toggleSoftKeyboard(
                        requireActivity(),
                        it1, false
                    )
                }
                dismiss()
            }
        }
    }

    private fun toggleSoftKeyboard(context: Context, view: View, isShow: Boolean) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (isShow) {
            view.requestFocus()
            imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
        } else {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    interface SubmitAction {
        fun submit(shell: String)
    }

    private var action: SubmitAction? = null
    fun setAction(ac: SubmitAction): EditShellDialogFragment {
        this.action = ac
        return this
    }


    companion object {
        fun newInstance(args: Bundle?): EditShellDialogFragment? {
            val fragment = EditShellDialogFragment()
            if (args != null) {
                fragment.arguments = args
            }
            return fragment
        }
    }
}