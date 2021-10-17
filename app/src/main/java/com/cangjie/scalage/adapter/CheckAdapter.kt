package com.cangjie.scalage.adapter

import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.LayoutCheckItemBinding
import com.cangjie.scalage.entity.GoodsInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/15 08:47
 */
class CheckAdapter :
    BaseQuickAdapter<GoodsInfo, BaseDataBindingHolder<LayoutCheckItemBinding>>(R.layout.layout_check_item) {

    private var selectPosition = 0

    fun checkPosition(position: Int) {
        selectPosition = position
        notifyDataSetChanged()
    }


    override fun convert(holder: BaseDataBindingHolder<LayoutCheckItemBinding>, item: GoodsInfo) {
        holder.dataBinding?.let {
            it.tvOrderNo.text = (getItemPosition(item) + 1).toString()
            it.info = item
            holder.itemView.isSelected = item.isRepair
            it.calType =
                if (item.deliver_unit.contains("斤") || item.deliver_unit.contains("公斤") || item.deliver_unit.contains(
                        "千克"
                    ) || item.deliver_unit.contains("克") || item.deliver_unit.contains("两")
                ) {
                    "计重"
                } else {
                    "计数"
                }


        }
    }
}