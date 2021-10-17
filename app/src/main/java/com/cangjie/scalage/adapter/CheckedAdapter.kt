package com.cangjie.scalage.adapter

import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.LayoutCheckedItemBinding
import com.cangjie.scalage.entity.GoodsInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/15 08:47
 */
class CheckedAdapter(action: Action) :
    BaseQuickAdapter<GoodsInfo, BaseDataBindingHolder<LayoutCheckedItemBinding>>(R.layout.layout_checked_item) {

    private val againAction: Action = action
    override fun convert(holder: BaseDataBindingHolder<LayoutCheckedItemBinding>, item: GoodsInfo) {
        holder.dataBinding?.let {
            it.tvOrderNo.text = (getItemPosition(item) + 1).toString()
            it.info = item
            it.submitAgain.setOnClickListener {
                againAction.action(item)
            }
            it.stockMode = if (item.stock_mode == 0) {
                "即入即出"
            } else {
                "先入后出"
            }
        }
    }

    interface Action {
        fun action(item: GoodsInfo)
    }
}