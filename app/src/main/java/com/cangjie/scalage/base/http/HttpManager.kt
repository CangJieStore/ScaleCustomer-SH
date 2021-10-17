package com.cangjie.scalage.base.http

import android.app.Application
import com.cangjie.scalage.BuildConfig
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.cookie.CookieStore
import rxhttp.wrapper.ssl.HttpsUtils
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * User: ljx
 * Date: 2019-11-26
 * Time: 20:44
 */
object HttpManager {

    fun init(context: Application?) {
        val file = File(context!!.externalCacheDir, "FoodCookie")
        val sslParams = HttpsUtils.getSslSocketFactory()
        val client = OkHttpClient.Builder()
            .cookieJar(CookieStore(file))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams!!.sSLSocketFactory, sslParams.trustManager)
            .hostnameVerifier(HostnameVerifier { _: String?, _: SSLSession? -> true })
            .build()
        val cacheFile = File(context.externalCacheDir, "FoodCache")
        RxHttpPlugins.init(client).setDebug(BuildConfig.DEBUG).setExcludeCacheKeys("time").setCache(
            cacheFile,
            1000 * 100.toLong(),
            CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE
        )
    }
}