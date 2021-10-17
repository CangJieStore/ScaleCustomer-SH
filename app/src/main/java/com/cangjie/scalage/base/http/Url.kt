package com.cangjie.scalage.base.http

import rxhttp.wrapper.annotation.DefaultDomain

object Url {

    @JvmField
    @DefaultDomain
    var baseUrl = "https://st.shian360.com/"

    const val update = "openapi/?op=upgrade"
    const val login = "openapi/?op=login"
    const val orders = "openapi/?op=order"
    const val submit = "openapi/?op=weight"
    const val upload = "openapi/?op=picture"
    const val again = "openapi/?op=repair"
    const val clear = "openapi/?op=clear"
}