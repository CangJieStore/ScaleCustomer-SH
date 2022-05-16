package com.cangjie.scalage.entity

import android.os.Parcelable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

/**
 * @author: guruohan
 * @date: 2021/11/20
 */
class UploadTask(
    val id: Long = 0,
    val goodsId: String,
    val batchId: String,
    val batchPath: String,
    var isUpload: Int,
    var progress: Int = 0,
    var disposable: Disposable?,
    var state: Int = 0,
    var message:String=""
) : Serializable