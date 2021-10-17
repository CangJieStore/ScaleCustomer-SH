package com.cangjie.scalage.core

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.cangjie.scalage.R
import com.cangjie.scalage.core.net.CJNet
import com.cangjie.scalage.core.net.CJNetManager
import com.cangjie.scalage.core.net.annotation.NetType
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/7 09:28
 */
abstract class BaseActivity<VB : ViewDataBinding> : AppCompatActivity(),
    CoroutineScope by MainScope() {

    protected val mBinding: VB by lazy {
        DataBindingUtil.setContentView(this, layoutId()) as VB
    }
    protected var mToolBar: Toolbar? = null

    override fun onStart() {
        super.onStart()
        CJNetManager.getInstance(this.application).register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityStackManager.addActivity(this)
        reloadToolbar()
        if (isImmersionBarEnabled) {
            initImmersionBar()
        }
        mBinding.lifecycleOwner = this
        initActivity(savedInstanceState)
    }


    protected fun setTitle(title: String?) {
        mToolBar?.title = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun reloadToolbar() {
        mToolBar = findViewById<View>(R.id.toolbar) as Toolbar?
        mToolBar?.let { setSupportActionBar(mToolBar) }
    }

    open fun initImmersionBar() {
        mToolBar?.let {
            setSupportActionBar(mToolBar)
            ImmersionBar.with(this).titleBar(mToolBar).init()
        }
    }

    open val isImmersionBarEnabled: Boolean
        get() = true

    override fun onDestroy() {
        super.onDestroy()
        CJNetManager.getInstance(this.application).unRegister(this)
        cancel()
        mBinding.unbind()
        ActivityStackManager.removeActivity(this)
    }

    @CJNet
    fun onNetStatusChange(str: @NetType String) {
        if (str == "NONE") {
            netStatus(0)
        } else {
            netStatus(1)
        }
    }

    open fun netStatus(status: Int) {

    }


    abstract fun layoutId(): Int
    abstract fun initActivity(savedInstanceState: Bundle?)


}