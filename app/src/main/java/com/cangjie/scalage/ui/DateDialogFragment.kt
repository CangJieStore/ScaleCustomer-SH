package com.cangjie.scalage.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.FragmentChooseDateBinding
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.destroyImmersionBar
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/15 09:35
 */
class DateDialogFragment : DialogFragment() {


    private var dataBinding: FragmentChooseDateBinding? = null
    private var currentDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.PreviewDialog)
    }


    override fun onStart() {
        super.onStart()
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            init()
        }
        dialog!!.setCanceledOnTouchOutside(false)
        val dialogWindow = dialog!!.window
        dialogWindow!!.setGravity(Gravity.CENTER)
        val lp = dialogWindow.attributes
        val displayMetrics = requireContext().resources.displayMetrics
        lp.width = (displayMetrics.widthPixels * 0.6f).toInt()
        dialogWindow.attributes = lp
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dataBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_choose_date, container, false)
        return dataBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding?.let {
            it.monthCalendar.setOnCalendarChangedListener { _, year, month, localDate, _ ->
                dataBinding!!.tvCurrentDate.text = year.toString() + "年" + month.toString() + "月"
                currentDate = localDate.toString()
            }
            it.actionCancel.setOnClickListener { dismiss() }
            it.actionDone.setOnClickListener {
                action?.submit(currentDate!!)
                dismissAllowingStateLoss()
            }
        }


    }

    interface SubmitAction {
        fun submit(date: String)
    }

    private var action: SubmitAction? = null
    fun setAction(ac: SubmitAction): DateDialogFragment {
        this.action = ac
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.let { destroyImmersionBar(it) }
    }


    companion object {
        fun newInstance(args: Bundle?): DateDialogFragment {
            val fragment = DateDialogFragment()
            if (args != null) {
                fragment.arguments = args
            }
            return fragment
        }
    }
}