package com.cangjie.scalage.base

import android.app.Application
import com.blankj.utilcode.util.ViewUtils
import com.cangjie.scalage.R
import com.cangjie.scalage.base.http.HttpManager
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.core.loading.LoadingConfig
import com.cangjie.scalage.core.loading.LoadingDialog
import com.cangjie.scalage.entity.UpdateModel
import com.cangjie.scalage.kit.OkHttp3Connection
import com.cangjie.scalage.kit.lib.ToastUtils
import com.cangjie.scalage.kit.lib.style.BlackToastStyle
import com.cangjie.scalage.kit.update.model.TypeConfig
import com.cangjie.scalage.kit.update.model.UpdateConfig
import com.cangjie.scalage.kit.update.utils.AppUpdateUtils
import com.cangjie.scalage.kit.update.utils.SSLUtils
import com.cangjie.scalage.scale.ScaleModule
import com.cangjie.scalage.scale.SerialPortUtilForScale
import com.umeng.commonsdk.UMConfigure
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

/**
 * @author CangJie
 * @date 2021/9/9 16:02
 */
class ScaleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setApplication(this)
        ToastUtils.init(this, BlackToastStyle())
        SerialPortUtilForScale.Instance().OpenSerialPort() //打开称重串口
        UMConfigure.setLogEnabled(true);
        //友盟预初始化
        UMConfigure.preInit(this, "61931c62e0f9bb492b5d5afb", "ScalaCustomer")
        UMConfigure.init(
            this,
            "61931c62e0f9bb492b5d5afb",
            "ScalaCustomer",
            UMConfigure.DEVICE_TYPE_PHONE,
            ""
        )
        CangJie.init(this)
        CangJie.config {
            multiProcess = true
            encryptKey = "encryptKey"
        }
        HttpManager.init(this)
        update()
    }

    private fun update() {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        builder.connectTimeout(30000, TimeUnit.SECONDS)
            .readTimeout(30000, TimeUnit.SECONDS)
            .writeTimeout(30000, TimeUnit.SECONDS)
            .sslSocketFactory(
                SSLUtils.getSslSocketFactory().sSLSocketFactory,
                SSLUtils.getSslSocketFactory().trustManager
            )
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .retryOnConnectionFailure(true)

        val updateConfig: UpdateConfig = UpdateConfig()
            .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_MODEL)
            .setShowNotification(true) //配置更新的过程中是否在通知栏显示进度
            .setNotificationIconRes(R.mipmap.download_icon) //配置通知栏显示的图标
            .setUiThemeType(TypeConfig.UI_THEME_L)
            .setAutoDownloadBackground(false)
            .setNeedFileMD5Check(false)
            .setCustomDownloadConnectionCreator(OkHttp3Connection.Creator(builder))
            .setModelClass(UpdateModel())
        AppUpdateUtils.init(this, updateConfig)
    }

    companion object {
        private var sInstance: Application? = null

        @Synchronized
        fun setApplication(application: Application) {
            sInstance = application
        }

        val instance: Application?
            get() {
                if (sInstance == null) {
                    throw NullPointerException("please inherit Application or call setApplication.")
                }
                return sInstance
            }
    }
}