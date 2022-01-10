package com.cangjie.scalage.base.http

import rxhttp.wrapper.annotation.DefaultDomain

object Url {

    @JvmField
    @DefaultDomain
    var baseUrl = "https://api.shian360.com/"

    const val update = "st/steelyard/?op=upgrade"
    const val login = "st/steelyard/?op=login"
    const val orders = "st/steelyard/?op=order"
    const val submit = "st/steelyard/?op=weight"
    const val upload = "st/steelyard/?op=picture"
    const val again = "st/steelyard/?op=repair"
    const val clear = "st/steelyard/?op=clear"
}