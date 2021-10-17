package com.cangjie.scalage.ui

import android.content.Intent
import android.os.Bundle
import com.cangjie.scalage.BR
import com.cangjie.scalage.R
import com.cangjie.scalage.core.BaseMvvmActivity
import com.cangjie.scalage.core.event.MsgEvent
import com.cangjie.scalage.databinding.ActivityLoginBinding
import com.cangjie.scalage.kit.show
import com.cangjie.scalage.vm.ScaleViewModel
import com.gyf.immersionbar.ktx.immersionBar

/**
 * @author: guruohan
 * @date: 2021/9/9
 */
class LoginActivity : BaseMvvmActivity<ActivityLoginBinding, ScaleViewModel>() {

    override fun initActivity(savedInstanceState: Bundle?) {
        mBinding.tvExit.setOnClickListener {
            finish()
        }
    }

    override fun initVariableId(): Int = BR.loginModel

    override fun layoutId(): Int = R.layout.activity_login
    override fun initImmersionBar() {
        super.initImmersionBar()
        immersionBar {
            fullScreen(true)
            statusBarDarkFont(true, 0.2f)
            init()
        }
    }

    override fun toast(notice: String?) {
        super.toast(notice)
        show(this, 2000, notice!!)
    }

    override fun loading(word: String?) {
        show(this, 2000, word!!)
    }

    override fun handleEvent(msg: MsgEvent) {
        super.handleEvent(msg)
        if (msg.code == 0) {
            startActivity(Intent(this, MainActivity::class.java))
            this@LoginActivity.finish()
        }
    }

}