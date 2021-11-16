package com.cangjie.scalage.kit.update.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.cangjie.scalage.ui.SplashActivity


/**
 * @author: guruohan
 * @date: 2021/11/16
 */
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action: String = intent?.action as String
        val localPkgName: String = context!!.packageName //取得MyReceiver所在的App的包名
        val data: Uri = intent.data as Uri
        val installedPkgName: String = data.schemeSpecificPart //取得安装的Apk的包名，只在该app覆盖安装后自启动
        if ((action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REPLACED) && installedPkgName == localPkgName) {
            val launchIntent = Intent(context, SplashActivity::class.java)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        }
    }
}