package com.cangjie.scalage.ui

import android.content.*
import android.content.res.Configuration
import android.graphics.*
import android.hardware.display.DisplayManager
import android.hardware.usb.UsbDevice
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Surface
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ViewUtils
import com.cangjie.scalage.BR
import com.cangjie.scalage.R
import com.cangjie.scalage.adapter.CheckAdapter
import com.cangjie.scalage.adapter.CheckedAdapter
import com.cangjie.scalage.adapter.ImageAdapter
import com.cangjie.scalage.base.workOnIO
import com.cangjie.scalage.core.BaseMvvmActivity
import com.cangjie.scalage.core.event.MsgEvent
import com.cangjie.scalage.databinding.ActivityCheckBinding
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.entity.GoodsInfo
import com.cangjie.scalage.entity.OrderInfo
import com.cangjie.scalage.entity.SubmitInfo
import com.cangjie.scalage.entity.UploadTask
import com.cangjie.scalage.kit.LuminosityAnalyzer
import com.cangjie.scalage.kit.RotationListener
import com.cangjie.scalage.kit.lib.ToastUtils
import com.cangjie.scalage.kit.show
import com.cangjie.scalage.scale.FormatUtil
import com.cangjie.scalage.scale.ScaleModule
import com.cangjie.scalage.service.MultiTaskUploader
import com.cangjie.scalage.vm.ScaleViewModel
import com.cangjie.uvccamera.UVCCameraHelper
import com.fondesa.recyclerviewdivider.dividerBuilder
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import com.serenegiant.usb.common.AbstractUVCCameraHandler.OnCaptureListener
import com.serenegiant.usb.widget.CameraViewInterface
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/14 22:23
 */
class CheckActivity : BaseMvvmActivity<ActivityCheckBinding, ScaleViewModel>() {

    //camera
    private var mCameraHelper: UVCCameraHelper? = null
    private var mUVCCameraView: CameraViewInterface? = null
    private var isRequest = false
    private var isPreview = false
    private lateinit var outputDirectory: File
    private var readDataReceiver: ReadDataReceiver? = null

    private var currentGoodsInfo: GoodsInfo? = null
    private var submitList = arrayListOf<SubmitInfo>()
    private var currentDeliveryType = 0
    private var currentOrder: OrderInfo? = null
    private var orderID: String? = null
    private var date: String? = null
    private var stockMode = -1
    private var currentShell: Float = 0.00F
    private var currentRepairGood: GoodsInfo? = null

    private val checkAdapter by lazy {
        CheckAdapter()
    }
    private val checkedAdapter by lazy {
        CheckedAdapter(object : CheckedAdapter.Action {
            override fun action(item: GoodsInfo) {
                currentRepairGood = item
                currentRepairGood!!.repair_receive = "1"
                viewModel.again(item.id)
            }

        })
    }

    private val imgAdapter by lazy {
        ImageAdapter()
    }
    private var imgData = arrayListOf<String>()

    override fun initActivity(savedInstanceState: Bundle?) {
        orderID = intent.getSerializableExtra("id") as String
        viewModel.loadDetail(orderID!!)
        date = intent.getStringExtra("date")
        hasCamera()
        dividerBuilder()
            .color(Color.parseColor("#cccccc"))
            .size(1, TypedValue.COMPLEX_UNIT_DIP)
            .showLastDivider()
            .build()
            .addTo(mBinding.ryOrderCheck)
        dividerBuilder()
            .color(Color.parseColor("#cccccc"))
            .size(1, TypedValue.COMPLEX_UNIT_DIP)
            .showLastDivider()
            .build()
            .addTo(mBinding.ryOrderChecked)
        mBinding.adapterCheck = checkAdapter
        mBinding.adapterChecked = checkedAdapter
        checkAdapter.setOnItemClickListener { adapter, _, position ->
            val choosePosition = adapter.data[position] as GoodsInfo
            checkPosition(choosePosition)
            if (choosePosition != currentGoodsInfo) {
                currentShell = 0.0F
                updateWeight()
                returnZero()
                submitList.clear()
                imgData.clear()
                imgAdapter.data.clear()
                imgAdapter.notifyDataSetChanged()
            }
            currentGoodsInfo = null
            currentGoodsInfo = choosePosition
            handlerSelected()
        }
        mBinding.editCurrentCount.setOnClickListener {
            EditPriceDialogFragment("本次数量", "请输入...").setContentCallback(object :
                EditPriceDialogFragment.ContentCallback {
                override fun content(content: String?) {
                    mBinding.editCurrentCount.text = content
                }
            }).show(supportFragmentManager)
        }
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mBinding.ryImg.layoutManager = linearLayoutManager
        mBinding.imgAdapter = imgAdapter
        imgAdapter.setOnItemClickListener { adapter, view, position ->
            val bundle = Bundle()
            bundle.putString("info", adapter.data[position] as String);
            PreviewCardDialogFragment.newInstance(bundle)!!
                .show(supportFragmentManager, "preview")
        }
        readDataReceiver = ReadDataReceiver()
        registerReceiver(readDataReceiver, IntentFilter(ScaleModule.WeightValueChanged))
        registerReceiver(readDataReceiver, IntentFilter(ScaleModule.ERROR))
    }

    private fun initWeight() {
        lifecycleScope.launch {
            workOnIO {
                try {
                    ScaleModule.Instance(this@CheckActivity) //初始化称重模块
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        show(this@CheckActivity, 2000, "初始化称重主板错误！")
                    }
                }
            }
        }
    }

    private fun calType(type: String): Int {
        return if (type.contains("斤")
            || type.contains("公斤")
            || type.contains("千克")
            || type.contains("克")
            || type.contains("两")
        ) {
            0
        } else {
            1
        }
    }

    private fun handlerSelected() {
        mBinding.tvDeliveryName.text = "商品名称：" + currentGoodsInfo!!.name
        mBinding.tvReceiveCount.text =
            "配送数量：" + currentGoodsInfo!!.deliver_quantity + currentGoodsInfo!!.deliver_unit
        mBinding.tvDeliveryCount.text =
            "已验数量：" + currentGoodsInfo!!.receive_quantity + currentGoodsInfo!!.deliver_unit
        mBinding.tvSacalUnit.text = currentGoodsInfo!!.deliver_unit
        mBinding.tvInputUnit.text = currentGoodsInfo!!.deliver_unit
        if (calType(currentGoodsInfo!!.deliver_unit) == 0) {
            mBinding.tvDeliveryType.text = "计量方式：计重"
            mBinding.tvDeliveryCurrent.visibility = View.VISIBLE
            mBinding.tvDeliveryCurrent.text = mBinding.tvCurrentWeight.text.toString()
            mBinding.llEditCount.visibility = View.GONE
            currentDeliveryType = 1
            mBinding.btnRemove.visibility = View.VISIBLE
            mBinding.btnResetZero.visibility = View.VISIBLE
        } else {
            mBinding.tvDeliveryType.text = "计量方式：计数"
            currentDeliveryType = 2
            mBinding.tvDeliveryCurrent.visibility = View.GONE
            mBinding.tvCurrentWeight.text = "0.00"
            mBinding.editCurrentCount.text = ""
            mBinding.llEditCount.visibility = View.VISIBLE
            mBinding.btnRemove.visibility = View.GONE
            mBinding.btnResetZero.visibility = View.GONE
        }
    }

    override fun initVariableId(): Int = BR.checkModel

    override fun layoutId(): Int = R.layout.activity_check
    override fun initImmersionBar() {
        super.initImmersionBar()
        immersionBar {
            fullScreen(true)
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            statusBarDarkFont(false)
            init()
        }
    }

    override fun onStart() {
        super.onStart()
        mCameraHelper?.registerUSB()
        initWeight()
    }

    private fun formatUnit(currentWeight: String): String {
        if (currentGoodsInfo != null && currentWeight.isNotEmpty()) {
            return when (currentGoodsInfo!!.deliver_unit) {
                "斤" -> {
                    (FormatUtil.roundByScale(
                        currentWeight.toDouble() * 2 - currentShell,
                        2
                    )).toString()
                }
                "克" -> {
                    (FormatUtil.roundByScale(currentWeight.toDouble() * 1000 - currentShell, 2))
                        .toString()
                }
                else -> {
                    currentWeight
                }
            }
        }
        return currentWeight
    }

    override fun handleEvent(msg: MsgEvent) {
        super.handleEvent(msg)
        when (msg.code) {
            3 -> {//finish
                finish()
            }
            4 -> {//restart delivery
                currentGoodsInfo?.let {
                    for (path in imgData) {
                        val file = File(path)
                        contentResolver.delete(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            MediaStore.Images.Media.DATA + "=?",
                            arrayOf(path)
                        )
                        file.delete()
                    }
                    currentRepairGood = it
                    viewModel.clear(it.id)
                    mBinding.tvDeliveryCount.text =
                        "已验数量：" + FormatUtil.roundByScale(
                            it.receive_quantity.toDouble(),
                            2
                        ) + it.deliver_unit
                    mBinding.editCurrentCount.text = ""
                    submitList.clear()
                    imgData.clear()
                    imgAdapter.data.clear()
                    imgAdapter.notifyDataSetChanged()
                }
            }
            5 -> {//submit delivery
                currentRepairGood = null
                if (mBinding.llEditCount.visibility == View.VISIBLE) {
                    if (submitList.size == 0) {
                        show(this@CheckActivity, 2000, "请拍照记录")
                        return
                    }
                }
                if (submitList.size == 0) {
                    show(this@CheckActivity, 2000, "请先验收才能完成提交")
                    return
                }
                val bundle = Bundle()
                bundle.putSerializable("info", submitList)
                SubmitDialogFragment.newInstance(bundle)!!
                    .setAction(object : SubmitDialogFragment.SubmitAction {
                        override fun submit(id: String, count: String, type: Int) {
                            stockMode = type
                            viewModel.submit(
                                id,
                                count,
                                type.toString(),
                                currentGoodsInfo!!.repair_receive
                            )
                        }
                    }).show(supportFragmentManager, "submit")

            }
            7 -> {//take photo
                currentGoodsInfo?.let {
                    if (currentDeliveryType == 2) {
                        if (TextUtils.isEmpty(mBinding.editCurrentCount.text.toString())) {
                            if (mBinding.editCurrentCount.text.toString().isEmpty()) {
                                show(this@CheckActivity, 2000, "请输入验收数量")
                                return
                            }
                        }
                    }
                    val currentNum = deliveryCount()
                    if (isPreview) {
                        takePicture(currentNum)
                    }
//                    takePhoto(currentNum)
                }
            }
            200 -> {//submit response
                for (item in submitList) {
                    viewModel.add(
                        SubmitOrder(
                            goodsId = currentGoodsInfo!!.id,
                            batchId = item.batch,
                            batchPath = item.batch_path,
                            isUpload = 1
                        )
                    )
                }
                viewModel.loadDetail(orderID!!)
                toast("提交成功")
                submitList.clear()
                imgData.clear()
                imgAdapter.data.clear()
                imgAdapter.notifyDataSetChanged()
            }
            300 -> {
                viewModel.showStatusFiled.set(0)
                viewModel.loadDetail(orderID!!)
            }
            119 -> {
                showShell()
            }
            223 -> {
                viewModel.loadDetail(orderID!!)
                currentGoodsInfo!!.receive_quantity = "0.00"
                currentGoodsInfo!!.batch = ""
                mBinding.tvDeliveryCount.text =
                    "已验数量：0.00" + currentGoodsInfo!!.deliver_unit
                checkedAdapter.remove(currentGoodsInfo!!)
                checkedAdapter.notifyDataSetChanged()

            }
            700 -> {
                currentShell = 0.0f
                returnZero()
                updateWeight()
            }
        }
    }

    private fun showShell() {
        EditPriceDialogFragment("手动去皮", "请输入皮重...").setContentCallback(object :
            EditPriceDialogFragment.ContentCallback {
            override fun content(content: String?) {
                currentShell = if (content.isNullOrEmpty()) 0f else content.toFloat()
                updateWeight()
            }
        }).show(supportFragmentManager)
    }

    override fun toast(notice: String?) {
        super.toast(notice)
        show(this, 2000, notice!!)
    }


    override fun onDestroy() {
        super.onDestroy()
        readDataReceiver?.let {
            unregisterReceiver(it)
        }
        mCameraHelper?.release()
    }


    private fun updateCameraUi() {
        lifecycleScope.launch(Dispatchers.IO) {
            outputDirectory.listFiles { file ->
                file == null
            }.maxOrNull()?.let {
                setGalleryThumbnail(Uri.fromFile(it))
            }
        }
    }

    private fun returnZero() {
        try {
            ScaleModule.Instance(this).ZeroClear()
        } catch (e: Exception) {
            ViewUtils.runOnUiThread {
                ToastUtils.show("置零失败")
            }
        }
    }

    private fun takePicture(currentWeight: String) {
        loading("处理中...")
        if (mCameraHelper == null || !mCameraHelper!!.isCameraOpened) {
            toast("摄像头连接失败!")
            return
        }
        val photoFile = createFile(
            outputDirectory,
            getString(R.string.output_photo_date_template),
            getString(R.string.output_photo_ext)
        )
        mCameraHelper!!.capturePicture(photoFile.absolutePath,
            OnCaptureListener { path ->
                if (TextUtils.isEmpty(path)) {
                    return@OnCaptureListener
                }
                val savedUri = Uri.fromFile(photoFile)
                val createTimeSdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var reBatch = 0
                if (currentGoodsInfo!!.batch.isNotEmpty()) {
                    reBatch = currentGoodsInfo!!.batch.toInt() + 1
                }
                val labels: MutableList<String> =
                    ArrayList()
                labels.add("订单编号:" + currentOrder!!.trade_no)
                labels.add("验收时间:" + createTimeSdf1.format(Date()))
                labels.add("商品名称:" + currentGoodsInfo!!.name)
                labels.add("配送数量:" + currentGoodsInfo!!.deliver_quantity + currentGoodsInfo!!.deliver_unit)
                labels.add("验收批次:" + (imgData.size + reBatch + 1).toString())
                labels.add("本批数量:" + currentWeight + currentGoodsInfo!!.deliver_unit)
                makeWater(photoFile, labels, imgData.size + reBatch, currentWeight)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    sendBroadcast(
                        Intent(
                            android.hardware.Camera.ACTION_NEW_PICTURE,
                            savedUri
                        )
                    )
                }

                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(savedUri.toFile().extension)
                MediaScannerConnection.scanFile(
                    this@CheckActivity,
                    arrayOf(savedUri.toString()),
                    arrayOf(mimeType)
                ) { _, uri ->
                }
            })
    }


    private fun takePhoto(currentWeight: String) {
//        imageCapture?.let { imageCapture ->
        val photoFile = createFile(
            outputDirectory,
            getString(R.string.output_photo_date_template),
            getString(R.string.output_photo_ext)
        )
//            val metadata = ImageCapture.Metadata().apply {
//                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
//            }

//            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
//                .setMetadata(metadata)
//                .build()

//            imageCapture.takePicture(
//                outputOptions,
//                cameraExecutor,
//                object : ImageCapture.OnImageSavedCallback {
//                    override fun onError(exc: ImageCaptureException) {
//                    }
//
//                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        val savedUri = Uri.fromFile(photoFile)
        val createTimeSdf1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var reBatch = 0
        if (currentGoodsInfo!!.batch.isNotEmpty()) {
            reBatch = currentGoodsInfo!!.batch.toInt() + 1
        }
        val labels: MutableList<String> =
            ArrayList()
        labels.add("订单编号:" + currentOrder!!.trade_no)
        labels.add("验收时间:" + createTimeSdf1.format(Date()))
        labels.add("商品名称:" + currentGoodsInfo!!.name)
        labels.add("配送数量:" + currentGoodsInfo!!.deliver_quantity + currentGoodsInfo!!.deliver_unit)
        labels.add("验收批次:" + (imgData.size + reBatch + 1).toString())
        labels.add("本批数量:" + currentWeight + currentGoodsInfo!!.deliver_unit)
        makeWater(photoFile, labels, imgData.size + reBatch, currentWeight)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sendBroadcast(
                Intent(
                    android.hardware.Camera.ACTION_NEW_PICTURE,
                    savedUri
                )
            )
        }

        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(savedUri.toFile().extension)
        MediaScannerConnection.scanFile(
            this@CheckActivity,
            arrayOf(savedUri.toString()),
            arrayOf(mimeType)
        ) { _, uri ->
        }
    }

    private fun setGalleryThumbnail(uri: Uri) {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }

    private fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder,
            getString(
                R.string.output_photo_name_template,
                SimpleDateFormat(
                    format,
                    Locale.CHINA
                ).format(System.currentTimeMillis()) + extension
            )
        )

    private fun createWaterFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder,
            getString(
                R.string.output_photo_water_template,
                SimpleDateFormat(
                    format,
                    Locale.CHINA
                ).format(System.currentTimeMillis()) + extension
            )
        )

    companion object {
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(
                    it,
                    appContext.resources.getString(R.string.output_photo_directory)
                ).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
        }
    }

    private fun makeWater(inputPath: File, labels: List<String>, batchID: Int, cWeight: String) {
        val single: Single<String> = Single.create { emitter ->
            val oldBitmap = BitmapFactory.decodeStream(FileInputStream(inputPath))
            val waterBitmap =
                addTimeFlag(this@CheckActivity, labels, 0, 20, "#ffffff", oldBitmap)
            val file = createWaterFile(
                outputDirectory,
                getString(R.string.output_photo_date_template),
                getString(R.string.output_photo_ext)
            )
            val os: OutputStream
            var ret = false
            try {
                os = BufferedOutputStream(FileOutputStream(file))
                ret = waterBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, os)
                waterBitmap.recycle()
                os.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (ret) {
                emitter.onSuccess(file.path)
            }
        }
        single.subscribe(object : SingleObserver<String> {
            override fun onSuccess(path: String) {
                dismissLoading()
                inputPath.delete()
                submitList.add(
                    SubmitInfo(
                        currentGoodsInfo!!.id,
                        (batchID + 1).toString(),
                        cWeight, path,
                        currentGoodsInfo!!.name,
                        currentGoodsInfo!!.deliver_quantity,
                        currentGoodsInfo!!.deliver_quantity,
                        currentGoodsInfo!!.deliver_unit
                    )
                )
                imgData.add(path)
                runOnUiThread {
                    mBinding.editCurrentCount.text = ""
                    mBinding.tvDeliveryCount.text =
                        "已验数量：" + getDeliveryCount() + currentGoodsInfo!!.deliver_unit
                    imgAdapter.setList(imgData)
                    mBinding.ryImg.smoothScrollToPosition(imgAdapter.itemCount - 1)
                }
            }

            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {
            }
        })
    }


    private fun deliveryCount(): String {
        return if (currentDeliveryType == 1) {
            mBinding.tvCurrentWeight.text.toString()
        } else {
            mBinding.editCurrentCount.text.toString()
        }
    }

    private fun getDeliveryCount(): String {
        var count = 0.00
        currentGoodsInfo?.let {
            count += it.receive_quantity.toDouble()
            for (item in submitList) {
                if (!TextUtils.isEmpty(item.batch_count)) {
                    count += item.batch_count.toFloat()
                }
            }
        }
        return FormatUtil.roundByScale(count, 2).toString()
    }


    private fun addTimeFlag(
        context: Context,
        labels: List<String>,
        degress: Int,
        fontSize: Int,
        fontColor: String,
        src: Bitmap
    ): Bitmap? {
        val width = src.width
        val height = src.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(src, 0f, 0f, null)
        val paint = Paint()
        paint.color = Color.parseColor(fontColor)
        paint.isAntiAlias = true
        paint.textSize = sp2px(context, fontSize.toFloat()).toFloat()
        paint.alpha = 220
        canvas.save()
        canvas.rotate(degress.toFloat())
        var spacing = 0
        for (label in labels) {
            Log.e("label", label);
            canvas.drawText(label, 20f, 35f + spacing.toFloat(), paint)
            spacing += 25
        }
        canvas.restore()
        return newBitmap
    }

    private fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F3) {//置零
            currentShell = 0.0F
            returnZero()
            updateWeight()
        } else if (keyCode == KeyEvent.KEYCODE_Y) {
            showShell()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun subscribeModel(model: ScaleViewModel) {
        super.subscribeModel(model)
        model.currentOrder.observe(this, {
            it?.let {
                currentOrder = it
                mBinding.info = currentOrder
                checkedAdapter.setList(it.goods.filter { value -> value.receive_quantity.toFloat() > 0f })
                if (it.goods.size > 0) {
                    if (currentRepairGood != null) {
                        currentGoodsInfo = currentRepairGood
                        currentGoodsInfo!!.repair_receive = currentRepairGood!!.repair_receive
                        it.goods.forEach { value ->
                            value.isRepair = value.id == currentRepairGood!!.id
                        }
                        checkAdapter.setList(it.goods.filter { value -> value.receive_quantity.toFloat() == 0f || value.repair_receive == "1" })
                        handlerSelected()
                    } else {
                        checkAdapter.setList(it.goods.filter { value -> value.receive_quantity.toFloat() == 0f || value.repair_receive == "1" })
                        if (checkAdapter.data.size > 0) {
                            currentGoodsInfo = checkAdapter.data[0]
                            handlerSelected()
                            checkPosition(currentGoodsInfo!!)
                        } else {
                            currentGoodsInfo = null
                            resetCheck()
                            viewModel.getUpload()
                        }
                    }
                }
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
                bundle.putInt("type", 0)
                val intent = Intent(this@CheckActivity, UploadImgActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }
        })
    }

    private fun checkPosition(item: GoodsInfo) {
        for (i in 0 until checkAdapter.data.size) {
            val goodsInfo = checkAdapter.data[i]
            goodsInfo.isRepair = goodsInfo == item
            checkAdapter.notifyItemChanged(i)
        }
    }

    inner class ReadDataReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ScaleModule.ERROR == intent.action) {
                val error = intent.getStringExtra("error")
                ToastUtils.show(error)
            } else {
                updateWeight()
            }
        }
    }


    private fun updateWeight() {
        try {
            val currentWeight = FormatUtil.roundByScale(
                ScaleModule.Instance(this@CheckActivity).RawValue - ScaleModule.Instance(this@CheckActivity).TareWeight,
                ScaleModule.Instance(this@CheckActivity).SetDotPoint
            )
            currentGoodsInfo?.let {
                if (currentDeliveryType == 1) {
                    mBinding.tvDeliveryCurrent.text = formatUnit(currentWeight)
                    mBinding.tvCurrentWeight.text = formatUnit(currentWeight)
                } else {
                    mBinding.tvCurrentWeight.text = "0.00"
                    mBinding.tvDeliveryCurrent.text = "0.00"
                }
            }
            currentGoodsInfo ?: resetCheck()
        } catch (ee: java.lang.Exception) {
            ee.printStackTrace()
            ToastUtils.show(ee.message!!)
        }
    }

    private fun resetCheck() {
        mBinding.tvCurrentWeight.text = "0.00"
        mBinding.tvDeliveryCurrent.text = "0.00"
        mBinding.tvDeliveryName.text = "商品名称：****"
        mBinding.tvDeliveryCount.text = "配送数量：0.00"
        mBinding.tvReceiveCount.text = "已验数量：0.00"
        mBinding.tvDeliveryCurrent.visibility = View.VISIBLE
        mBinding.llEditCount.visibility = View.GONE
        mBinding.btnRemove.visibility = View.GONE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            init()
        }
    }

    override fun onStop() {
        super.onStop()
        mCameraHelper?.unregisterUSB()
    }

    private fun hasCamera() {
        outputDirectory = getOutputDirectory(this)
        mCameraHelper = UVCCameraHelper.getInstance()
        mUVCCameraView = mBinding.hasCamera as CameraViewInterface
        mUVCCameraView?.let {
            it.setCallback(object : CameraViewInterface.Callback {
                override fun onSurfaceCreated(view: CameraViewInterface?, surface: Surface?) {
                    if (!isPreview && mCameraHelper!!.isCameraOpened) {
                        mCameraHelper!!.startPreview(mUVCCameraView)
                        isPreview = true
                    }
                }

                override fun onSurfaceChanged(
                    view: CameraViewInterface?,
                    surface: Surface?,
                    width: Int,
                    height: Int
                ) {

                }

                override fun onSurfaceDestroy(view: CameraViewInterface?, surface: Surface?) {
                    if (isPreview && mCameraHelper!!.isCameraOpened) {
                        mCameraHelper!!.stopPreview()
                        isPreview = false
                    }
                }
            })
        }
        mCameraHelper?.let {
            it.setDefaultPreviewSize(1280, 720)
            it.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG)
            it.initUSBMonitor(
                this@CheckActivity,
                mUVCCameraView,
                object : UVCCameraHelper.OnMyDevConnectListener {
                    override fun onAttachDev(device: UsbDevice?) {
                        if (!isRequest) {
                            isRequest = true
                            mCameraHelper?.requestPermission(0)
                        }
                    }

                    override fun onDettachDev(device: UsbDevice?) {
                        // close camera
                        if (isRequest) {
                            isRequest = false
                            mCameraHelper?.closeCamera()
                        }
                    }

                    override fun onConnectDev(device: UsbDevice?, isConnected: Boolean) {
                        isPreview = isConnected
                        if (isConnected) {
                            runOnUiThread {
                                Handler().postDelayed({
                                    mBinding.pbStarting.visibility = View.GONE
                                }, 1500)
                            }
                        }
                    }

                    override fun onDisConnectDev(device: UsbDevice?) {

                    }
                })
        }
    }
}