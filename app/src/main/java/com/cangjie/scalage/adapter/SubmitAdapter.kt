package com.cangjie.scalage.adapter

import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.LayoutSubmitItemBinding
import com.cangjie.scalage.entity.SubmitInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/16 14:18
 */
class SubmitAdapter :
    BaseQuickAdapter<SubmitInfo, BaseDataBindingHolder<LayoutSubmitItemBinding>>(R.layout.layout_submit_item) {
    override fun convert(holder: BaseDataBindingHolder<LayoutSubmitItemBinding>, item: SubmitInfo) {
        holder.dataBinding?.info = item
    }
}