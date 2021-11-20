package com.cangjie.scalage.ui

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cangjie.scalage.BR
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.UploadImageAdapter
import com.cangjie.scalage.base.BaseFragmentPagerAdapter
import com.cangjie.scalage.base.DateUtil
import com.cangjie.scalage.base.workOnIO
import com.cangjie.scalage.core.BaseMvvmActivity
import com.cangjie.scalage.core.event.MsgEvent
import com.cangjie.scalage.databinding.ActivityMainBinding
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.entity.ListModel
import com.cangjie.scalage.entity.MessageEvent
import com.cangjie.scalage.entity.Update
import com.cangjie.scalage.entity.UploadTask
import com.cangjie.scalage.kit.show
import com.cangjie.scalage.kit.update.model.DownloadInfo
import com.cangjie.scalage.kit.update.model.TypeConfig
import com.cangjie.scalage.kit.update.utils.AppUpdateUtils
import com.cangjie.scalage.scale.SerialPortUtilForScale
import com.cangjie.scalage.service.InitService
import com.cangjie.scalage.service.MultiTaskUploader
import com.cangjie.scalage.vm.ScaleViewModel
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : BaseMvvmActivity<ActivityMainBinding, ScaleViewModel>() {

    override fun onStart() {
        super.onStart()
        startService(Intent(this, InitService::class.java))
    }

    private val title = arrayListOf("未验收", "已验收")
    private val mAdapter by lazy {
        BaseFragmentPagerAdapter(
            supportFragmentManager, arrayListOf(
                UncheckFragment(),
                CheckedFragment()
            ), title
        )
    }

    override fun initActivity(savedInstanceState: Bundle?) {
        mBinding.vpOrders.adapter = mAdapter
        mBinding.tabOrders.setViewPager(mBinding.vpOrders)
        mBinding.tabOrders.currentTab = 0
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date(System.currentTimeMillis())
        viewModel.chooseDateFiled.set(simpleDateFormat.format(date))
        netTime()
        viewModel.getUpload()
    }

    private fun netTime() {
        lifecycleScope.launch {
            workOnIO {
                try {
                    val infoUrl = URL("http://www.baidu.com")
                    val connection = infoUrl.openConnection()
                    connection.connect()
                    val ld = connection.date
                    val now = DateUtil.dateToString(Date(ld), DateUtil.DATE_FORMAT)
                    viewModel.chooseDateFiled.set(now)
                    EventBus.getDefault().post(MessageEvent(0, now))
                } catch (e: Exception) {
                    EventBus.getDefault()
                        .post(MessageEvent(0, viewModel.chooseDateFiled.get().toString()))
                }
            }
        }
    }

    override fun initVariableId(): Int = BR.mainModel

    override fun layoutId(): Int = R.layout.activity_main

    override fun initImmersionBar() {
        super.initImmersionBar()
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            statusBarDarkFont(false)
            init()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            init()
        }
    }

    override fun toast(notice: String?) {
        super.toast(notice)
        show(this, 2000, notice!!)
    }

    override fun handleEvent(msg: MsgEvent) {
        super.handleEvent(msg)
        when (msg.code) {
            1 -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            2 -> {
                val bundle = Bundle()
                bundle.putString("date", viewModel.chooseDateFiled.get())
                DateDialogFragment.newInstance(bundle)
                    .setAction(object : DateDialogFragment.SubmitAction {
                        override fun submit(date: String) {
                            viewModel.chooseDateFiled.set(date)
                            EventBus.getDefault()
                                .post(MessageEvent(0, date))
                        }
                    }).show(supportFragmentManager, "date")
            }
            6 -> {
                EventBus.getDefault()
                    .post(MessageEvent(0, viewModel.chooseDateFiled.get().toString()))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUpdate()
        EventBus.getDefault().post(MessageEvent(0, viewModel.chooseDateFiled.get().toString()))
    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, InitService::class.java))
    }

    private fun update(update: Update) {
        AppUpdateUtils.getInstance().clearAllData()
        val listModel = ListModel()
        listModel.isForceUpdate = update.forceUpdate
        listModel.uiTypeValue = TypeConfig.UI_THEME_L
        listModel.isCheckFileMD5 = true
        listModel.sourceTypeVaule = TypeConfig.DATA_SOURCE_TYPE_MODEL
        val info =
            DownloadInfo().setApkUrl(update.apkUrl)
                .setFileSize(31338250)
                .setProdVersionCode(update.versionCode)
                .setProdVersionName(update.versionName)
                .setMd5Check("68919BF998C29DA3F5BD2C0346281AC0")
                .setForceUpdateFlag(if (listModel.isForceUpdate) 1 else 0)
                .setUpdateLog(update.updateLog)
        AppUpdateUtils.getInstance().updateConfig.uiThemeType = listModel.uiTypeValue
        AppUpdateUtils.getInstance().updateConfig.isNeedFileMD5Check = false
        AppUpdateUtils.getInstance().updateConfig.dataSourceType = listModel.sourceTypeVaule
        AppUpdateUtils.getInstance().updateConfig.isAutoDownloadBackground =
            listModel.isAutoUpdateBackground
        AppUpdateUtils.getInstance().updateConfig.isShowNotification =
            !listModel.isAutoUpdateBackground
        AppUpdateUtils.getInstance().checkUpdate(info)
    }

    override fun subscribeModel(model: ScaleViewModel) {
        super.subscribeModel(model)
        model.getUpdate().observe(this, {
            it?.let {
                update(it)
            }
        })
        model.allUploadOrders.observe(this, {
            if (it.size > 0) {
                val data = arrayListOf<UploadTask>()
                it.forEach { item ->
                    run {
                        val task = UploadTask(
                            item.id,
                            item.goodsId,
                            item.batchId,
                            item.batchPath,
                            item.isUpload,
                            0,
                            null, MultiTaskUploader.IDLE
                        )
                        data.add(task)
                    }
                }
                val bundle = Bundle()
                bundle.putSerializable("orders", data)
                UploadDialogFragment.newInstance(bundle)
                    .setStandByCallback(object : UploadDialogFragment.StandByCallback {
                        override fun upload(item: UploadTask) {
                            val submitOrder =
                                SubmitOrder(item.id, item.goodsId, item.batchId, item.batchPath, 2)
                            val file = File(item.batchPath)
                            contentResolver.delete(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.Media.DATA + "=?",
                                arrayOf(item.batchPath)
                            )
                            file.delete()
                            viewModel.update(submitOrder)
                        }
                    }).show(supportFragmentManager, "")
            }
        })
    }

}