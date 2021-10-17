package com.cangjie.scalage.adapter

import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.LayoutGoodsItemBinding
import com.cangjie.scalage.entity.GoodsInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/14 21:46
 */
class DetailAdapter(preview: PreviewAction) :
    BaseQuickAdapter<GoodsInfo, BaseDataBindingHolder<LayoutGoodsItemBinding>>(R.layout.layout_goods_item) {
    private val previewAction = preview
    override fun convert(holder: BaseDataBindingHolder<LayoutGoodsItemBinding>, item: GoodsInfo) {
        holder.dataBinding?.let {
            it.info = item
            it.tvDetailNo.text = (getItemPosition(item) + 1).toString()
            it.path = item.path
            it.ivGoods.setOnClickListener {
                previewAction.preview(item.path)
            }
            it.tvSubmitGain.setOnClickListener {
                previewAction.again(item.id)
            }
            it.stockMode = if (item.stock_mode == 0) {
                "即入即出"
            } else {
                "先入后出"
            }
        }
    }

    interface PreviewAction {
        fun preview(path: String)
        fun again(goodsId:String)
    }
}