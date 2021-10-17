package com.cangjie.scalage.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cangjie.scalage.ui.SplashActivity


class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if ("android.intent.action.BOOT_COMPLETED" == p1!!.action) {
            val intent = Intent(p0!!, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            p0.startActivity(intent)
        }
    }

}