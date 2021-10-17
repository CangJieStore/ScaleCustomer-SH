package com.cangjie.scalage.entity

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/15 22:25
 */
data class MockGoodsInfo(
    val id: String,
    val name: String,
    val buy_unit: String,
    val buy_quantity: String,
    val format: String,
    val deliver_unit: String,
    val deliver_quantity: String,
    val receive_quantity: String,
    val stock_mode: String,
    var selected: Boolean,
    val loginType: Int
)