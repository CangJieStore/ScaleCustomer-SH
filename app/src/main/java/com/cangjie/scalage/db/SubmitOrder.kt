package com.cangjie.scalage.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "orders_table")
data class SubmitOrder(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    val goodsId: String,
    val batchId: String,
    val batchPath: String,
    var isUpload: Int
) : Parcelable