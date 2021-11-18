package com.cangjie.scalage.adapter

import android.provider.MediaStore
import android.view.View
import com.cangjie.scalage.R
import com.cangjie.scalage.base.http.Url
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.databinding.LayoutImgItemBinding
import com.cangjie.scalage.databinding.LayoutUploadItemBinding
import com.cangjie.scalage.db.SubmitOrder
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.RxHttp
import rxhttp.toFlow
import java.io.File

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/15 16:27
 */
class UploadImageAdapter :
    BaseQuickAdapter<SubmitOrder, BaseDataBindingHolder<LayoutUploadItemBinding>>(R.layout.layout_upload_item) {

    private var progress = 0
    override fun convert(
        holder: BaseDataBindingHolder<LayoutUploadItemBinding>,
        item: SubmitOrder
    ) {
        holder.dataBinding?.let {
            it.path = item.batchPath
            it.pos = (holder.adapterPosition + 1).toString()
            if (item.isUpload == 1) {
                it.tvStatus.visibility = View.GONE
            } else {
                it.tvStatus.visibility = View.VISIBLE
                it.pbUpload.progress = 100
            }
        }
    }

    fun updateProgress(pb: Int, pos: Int) {
        this.progress = pb
        notifyItemChanged(pos)
    }
}