package com.cangjie.scalage.adapter

import android.view.View
import com.cangjie.scalage.R
import com.cangjie.scalage.databinding.LayoutUploadItemBinding
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.entity.UploadTask
import com.cangjie.scalage.service.MultiTaskUploader
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/15 16:27
 */
class UploadImageAdapter :
    BaseQuickAdapter<UploadTask, BaseDataBindingHolder<LayoutUploadItemBinding>>(R.layout.layout_upload_item) {

    private var callback: DoneCallback? = null

    override fun convert(
        holder: BaseDataBindingHolder<LayoutUploadItemBinding>,
        item: UploadTask
    ) {
        holder.dataBinding?.let {
            it.path = item.batchPath
            it.pos = (holder.adapterPosition + 1).toString()
            it.pbUpload.progress = item.progress
            when (item.state) {
                MultiTaskUploader.IDLE -> {
                    it.tvStatus.text = "排队中..."
                }
                MultiTaskUploader.WAITING -> {
                    it.tvStatus.text = "等待上传..."
                }
                MultiTaskUploader.UPLODING -> {
                    it.tvStatus.text = "上传中..."
                }
                MultiTaskUploader.PAUSED -> {
                    it.tvStatus.text = "已暂停"
                }
                MultiTaskUploader.COMPLETED -> {
                    it.tvStatus.text = "上传成功"
                    callback?.done(item)
                }
                MultiTaskUploader.FAIL -> {
                    it.tvStatus.text = "上传失败"
                }
                MultiTaskUploader.CANCEL -> {
                    it.tvStatus.text = "已取消"
                }
                else -> {}
            }
        }
    }

    interface DoneCallback {
        fun done(item: UploadTask)
    }

    fun setDoneCallback(cb: DoneCallback) {
        this.callback = cb
    }
}