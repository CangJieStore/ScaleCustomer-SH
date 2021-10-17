package com.cangjie.scalage.entity

import java.io.Serializable

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/13 18:16
 */
data class GoodsInfo(
    val id: String,
    val name: String,
    val deliver_unit: String,
    val deliver_quantity: String,
    var receive_quantity: String,
    val stock_mode: Int,
    val spec: String,
    val receive_date: String,
    val path: String,
    var batch: String,
    val repair_receive: String,
    var isRepair: Boolean
) : Serializable