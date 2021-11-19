package com.cangjie.scalage.ui

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.UploadImageAdapter
import com.cangjie.scalage.databinding.DialogUploadImgBinding
import com.cangjie.scalage.db.SubmitOrder
import com.fondesa.recyclerviewdivider.dividerBuilder
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar


/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/15 09:35
 */
class UploadDialogFragment : DialogFragment() {


    private var dialogUploadImgBinding: DialogUploadImgBinding? = null
    private var standByCallback: StandByCallback? = null
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
        lp.width = (displayMetrics.widthPixels * 0.7f).toInt()
        dialogWindow.attributes = lp
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogUploadImgBinding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_upload_img, container, false)
        return dialogUploadImgBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            init()
        }
        val uploadOrders =
            arguments?.getParcelableArrayList<SubmitOrder>("orders") as ArrayList<SubmitOrder>
        val uploadImageAdapter = UploadImageAdapter()

        dialogUploadImgBinding?.let {
            requireActivity().dividerBuilder().color(Color.TRANSPARENT)
                .size(10, TypedValue.COMPLEX_UNIT_DIP)
                .showLastDivider()
                .showFirstDivider()
                .showSideDividers()
                .build()
                .addTo(it.ryImg)
            it.ryImg.adapter = uploadImageAdapter
            uploadImageAdapter.setList(uploadOrders)
            standByCallback?.let { cb ->
                run {
                    cb.upload(uploadImageAdapter)
                }
            }
        }
    }

    interface StandByCallback {
        fun upload(adapter: UploadImageAdapter)
    }

    fun setStandByCallback(cb: StandByCallback): UploadDialogFragment {
        this.standByCallback = cb
        return this
    }


    companion object {
        fun newInstance(args: Bundle?): UploadDialogFragment? {
            val fragment = UploadDialogFragment()
            if (args != null) {
                fragment.arguments = args
            }
            return fragment
        }
    }
}