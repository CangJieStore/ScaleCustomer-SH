package com.cangjie.scalage.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import com.cangjie.scalage.BR
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.DetailAdapter
import com.cangjie.scalage.core.BaseMvvmActivity
import com.cangjie.scalage.core.event.MsgEvent
import com.cangjie.scalage.databinding.ActivityCheckedDetailBinding
import com.cangjie.scalage.entity.OrderInfo
import com.cangjie.scalage.vm.ScaleViewModel
import com.fondesa.recyclerviewdivider.dividerBuilder
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/14 15:38
 */
class CheckedDetailActivity : BaseMvvmActivity<ActivityCheckedDetailBinding, ScaleViewModel>() {

    private var currentOrder: OrderInfo?=null

    private val detailAdapter by lazy {
        DetailAdapter(object : DetailAdapter.PreviewAction {
            override fun preview(path: String) {
                val bundle = Bundle()
                bundle.putString("info", path);
                PreviewCardDialogFragment.newInstance(bundle)!!
                    .show(supportFragmentManager, "preview")
            }

            override fun again(goodsId: String) {
                viewModel.again(goodsId)
            }
        })
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        currentOrder = intent.getSerializableExtra("info") as OrderInfo
        mBinding.info = currentOrder
        dividerBuilder()
            .color(Color.parseColor("#cccccc"))
            .size(1, TypedValue.COMPLEX_UNIT_DIP)
            .showLastDivider()
            .build()
            .addTo(mBinding.ryOrders)
        mBinding.adapter = detailAdapter
        detailAdapter.setList(currentOrder!!.goods)
    }

    override fun initVariableId(): Int = BR.detailModel

    override fun layoutId(): Int = R.layout.activity_checked_detail
    override fun initImmersionBar() {
        super.initImmersionBar()
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            statusBarDarkFont(false)
            init()
        }
    }

    override fun handleEvent(msg: MsgEvent) {
        super.handleEvent(msg)
        if (msg.code == 3) {
            finish()
        }else if(msg.code==300){
            val intent = Intent(this, CheckActivity::class.java)
            intent.putExtra("id", currentOrder!!.trade_no)
            startActivity(intent)
        }
    }
}