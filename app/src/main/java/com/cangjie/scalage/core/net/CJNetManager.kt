package com.cangjie.scalage.core.net

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import com.cangjie.scalage.core.net.annotation.NetType

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/14 10:32
 */
class CJNetManager(private val application: Application) {

    // 回调
    private val netStatusCallBack = NetStatusCallBack(application)

    companion object {

        @Volatile
        private var INSTANCE: CJNetManager? = null

        @JvmStatic
        fun getInstance(application: Application): CJNetManager {
            val temp = INSTANCE
            if (null != temp) {
                return temp
            }
            synchronized(this) {
                val instance = CJNetManager(application)
                INSTANCE = instance
                return instance
            }
        }
    }

    init {
        val request = NetworkRequest.Builder().build()
        val manager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(request, netStatusCallBack)
    }

    fun register(activity: Any) {
        netStatusCallBack.register(activity)
    }

    fun unRegister(activity: Any) {
        netStatusCallBack.unRegister(activity)
    }

    fun unRegisterAll() {
        netStatusCallBack.unRegisterAll()
        val manager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.unregisterNetworkCallback(netStatusCallBack)
    }

    fun getNetType(): @NetType String {
        return netStatusCallBack.getNetType()
    }
}