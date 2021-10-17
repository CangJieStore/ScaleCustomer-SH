package com.cangjie.scalage.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.cangjie.scalage.R
import com.cangjie.scalage.base.delayLaunch
import com.cangjie.scalage.core.BaseActivity
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.databinding.ActivitySplashBinding
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.coroutines.launch

/**
 * @author: guruohan
 * @date: 2021/9/9
 */
class SplashActivity:BaseActivity<ActivitySplashBinding>() {
    override fun initActivity(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            delayLaunch(2000) {
                if (CangJie.getString("token", "").isEmpty()) {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }
                finish()
            }

        }
    }


    override fun layoutId(): Int = R.layout.activity_splash
    override fun initImmersionBar() {
        super.initImmersionBar()
        immersionBar {
            fullScreen(true)
            statusBarDarkFont(true, 0.1f)
            init()
        }
    }
}