package com.cangjie.scalage.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.cangjie.scalage.BR
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.OrderAdapter
import com.cangjie.scalage.base.DateUtil
import com.cangjie.scalage.base.workOnIO
import com.cangjie.scalage.core.BaseMvvmFragment
import com.cangjie.scalage.databinding.FragmentUncheckBinding
import com.cangjie.scalage.entity.MessageEvent
import com.cangjie.scalage.entity.OrderInfo
import com.cangjie.scalage.kit.show
import com.cangjie.scalage.vm.ScaleViewModel
import com.fondesa.recyclerviewdivider.dividerBuilder
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URL
import java.util.*


/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/13 17:25
 */
class UncheckFragment : BaseMvvmFragment<FragmentUncheckBinding, ScaleViewModel>() {

    private val orderAdapter by lazy {
        OrderAdapter(0)
    }

    override fun initFragment(view: View, savedInstanceState: Bundle?) {
        requireActivity().dividerBuilder()
            .color(Color.parseColor("#cccccc"))
            .size(1, TypedValue.COMPLEX_UNIT_DIP)
            .showLastDivider()
            .build()
            .addTo(mBinding!!.ryOrders)
        netTime()
        mBinding!!.adapter = orderAdapter
        orderAdapter.setOnItemClickListener { adapter, view, position ->
            val intent = Intent(requireActivity(), CheckActivity::class.java)
            val info = adapter.data[position] as OrderInfo
            intent.putExtra("id", info.trade_no)
            startActivity(intent)
        }
    }

    private fun netTime() {
        lifecycleScope.launch {
            workOnIO {
                val infoUrl = URL("http://www.baidu.com")
                val connection = infoUrl.openConnection()
                connection.connect()
                val ld = connection.date
                val now = DateUtil.dateToString(Date(ld), DateUtil.DATE_FORMAT)
                viewModel.loadMain(now)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.code == 0) {
            viewModel.loadMain(event.msg)
        }
    }

    override fun initVariableId(): Int = BR.uncheckModel

    override fun layoutId(): Int = R.layout.fragment_uncheck
    override fun subscribeModel(model: ScaleViewModel) {
        super.subscribeModel(model)
        model.getOrderInfo().observe(this, Observer {
            it?.let {
                orderAdapter.setList(it.filter { it -> it.item_count != it.receive_item_count })
            }
        })
    }

    override fun toast(notice: String?) {
        super.toast(notice)
        show(requireActivity(), 2000, notice!!)
    }
}