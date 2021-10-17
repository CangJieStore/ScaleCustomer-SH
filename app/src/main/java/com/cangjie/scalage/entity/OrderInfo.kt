package com.cangjie.scalage.entity

import java.io.Serializable

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/13 18:16
 */
data class OrderInfo(
    val trade_no: String,
    val item_count: String,
    val receive_item_count: String,
    val deliver_quantity: String,
    val collect_time: String,
    val circulate_name: String,
    val goods: MutableList<GoodsInfo>
) : Serializable