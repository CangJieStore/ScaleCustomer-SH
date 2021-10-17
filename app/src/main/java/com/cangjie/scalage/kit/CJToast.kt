package com.cangjie.scalage.kit

import android.R
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cangjie.scalage.kit.draggable.BaseDraggable
import com.cangjie.scalage.kit.draggable.MovingDraggable


class CJToast private constructor(context: Context) {
    /**
     * 获取上下文对象
     */
    /** 上下文  */
    var context: Context?
        private set

    /**
     * 获取根布局
     */
    /** 根布局  */
    var view: View? = null
        private set

    /**
     * 获取 WindowManager 对象
     */
    /** 悬浮窗口  */
    var windowManager: WindowManager?
        private set

    /**
     * 获取 WindowManager 参数集
     */
    /** 窗口参数  */
    var windowParams: WindowManager.LayoutParams?
        private set

    /**
     * 当前是否已经显示
     */
    /** 当前是否已经显示  */
    var isShow = false
        private set

    /** 窗口显示时长  */
    private var mDuration = 0

    /** Toast 生命周期管理  */
    private var mLifecycle: ToastLifecycle? = null

    /** 自定义拖动处理  */
    private var mDraggable: BaseDraggable? = null

    /** 吐司显示和取消监听  */
    private var mListener: OnToastListener? = null

    /**
     * 创建一个局部悬浮窗
     */
    constructor(activity: Activity) : this(activity as Context) {
        if (activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN != 0) {
            // 如果当前 Activity 是全屏模式，那么需要添加这个标记，否则会导致 WindowManager 在某些机型上移动不到状态栏的位置上
            // 如果不想让状态栏显示的时候把 WindowManager 顶下来，可以添加 FLAG_LAYOUT_IN_SCREEN，但是会导致软键盘无法调整窗口位置
            addWindowFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        // 跟随 Activity 的生命周期
        mLifecycle = ToastLifecycle(this, activity)
    }

    /**
     * 创建一个全局悬浮窗
     */
    constructor(application: Application) : this(application as Context) {

        // 设置成全局的悬浮窗，注意需要先申请悬浮窗权限，推荐使用：https://github.com/getActivity/XXPermissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            setWindowType(WindowManager.LayoutParams.TYPE_PHONE)
        }
    }

    /**
     * 是否有这个标志位
     */
    fun hasWindowFlags(flags: Int): Boolean {
        return windowParams!!.flags and flags != 0
    }

    /**
     * 添加一个标记位
     */
    fun addWindowFlags(flags: Int): CJToast {
        windowParams!!.flags = windowParams!!.flags or flags
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 移除一个标记位
     */
    fun removeWindowFlags(flags: Int): CJToast {
        windowParams!!.flags = windowParams!!.flags and flags.inv()
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置标记位
     */
    fun setWindowFlags(flags: Int): CJToast {
        windowParams!!.flags = flags
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置窗口类型
     */
    fun setWindowType(type: Int): CJToast {
        windowParams!!.type = type
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置动画样式
     */
    fun setAnimStyle(id: Int): CJToast {
        windowParams!!.windowAnimations = id
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置随意拖动
     */
    fun setDraggable(): CJToast {
        return setDraggable(MovingDraggable())
    }

    /**
     * 设置拖动规则
     */
    fun setDraggable(draggable: BaseDraggable?): CJToast {
        // 当前是否设置了不可触摸，如果是就擦除掉这个标记
        if (hasWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)) {
            removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        mDraggable = draggable
        if (isShow) {
            update()
            mDraggable!!.start(this)
        }
        return this
    }

    /**
     * 设置宽度
     */
    fun setWidth(width: Int): CJToast {
        windowParams!!.width = width
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置高度
     */
    fun setHeight(height: Int): CJToast {
        windowParams!!.height = height
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 限定显示时长
     */
    fun setDuration(duration: Int): CJToast {
        mDuration = duration
        if (isShow) {
            if (mDuration != 0) {
                removeCallbacks()
                postDelayed(ToastDismissRunnable(this), mDuration.toLong())
            }
        }
        return this
    }

    /**
     * 设置监听
     */
    fun setOnToastListener(listener: OnToastListener?): CJToast {
        mListener = listener
        return this

    }

    /**
     * 设置窗口重心
     */
    fun setGravity(gravity: Int): CJToast {
        windowParams!!.gravity = gravity
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置窗口方向
     *
     * 自适应：ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
     * 横屏：ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
     * 竖屏：ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
     */
    fun setOrientation(orientation: Int): CJToast {
        windowParams!!.screenOrientation = orientation
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置 X 轴偏移量
     */
    fun setXOffset(x: Int): CJToast {
        windowParams!!.x = x
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置 Y 轴偏移量
     */
    fun setYOffset(y: Int): CJToast {
        windowParams!!.y = y
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置 WindowManager 参数集
     */
    fun setWindowParams(params: WindowManager.LayoutParams?): CJToast {
        windowParams = params
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 设置布局
     */
    fun setView(id: Int): CJToast {
        return setView(LayoutInflater.from(context).inflate(id, context?.let { FrameLayout(it) }, false))
    }

    fun setView(view: View?): CJToast {
        this.view = view
        val params = view!!.layoutParams
        if (params != null && windowParams!!.width == WindowManager.LayoutParams.WRAP_CONTENT && windowParams!!.height == WindowManager.LayoutParams.WRAP_CONTENT) {
            // 如果当前 Dialog 的宽高设置了自适应，就以布局中设置的宽高为主
            setWidth(params.width)
            setHeight(params.height)
        }

        // 如果当前没有设置重心，就自动获取布局重心
        if (windowParams!!.gravity == Gravity.NO_GRAVITY) {
            if (params is FrameLayout.LayoutParams) {
                setGravity(params.gravity)
            } else if (params is LinearLayout.LayoutParams) {
                setGravity(params.gravity)
            } else {
                // 默认重心是居中
                setGravity(Gravity.CENTER)
            }
        }
        if (isShow) {
            update()
        }
        return this
    }

    /**
     * 显示
     */
    fun show(): CJToast {
        require(!(view == null || windowParams == null)) { "WindowParams and view cannot be empty" }

        // 如果当前已经显示取消上一次显示
        if (isShow) {
            cancel()
        }
        try {
            // 如果这个 View 对象被重复添加到 WindowManager 则会抛出异常
            // java.lang.IllegalStateException: View android.widget.TextView{3d2cee7 V.ED..... ......ID 0,0-312,153} has already been added to the window manager.
            windowManager!!.addView(view, windowParams)
            // 当前已经显示
            isShow = true
            // 如果当前限定了显示时长
            if (mDuration != 0) {
                postDelayed(ToastDismissRunnable(this), mDuration.toLong())
            }
            // 如果设置了拖拽规则
            if (mDraggable != null) {
                mDraggable!!.start(this)
            }

            // 注册 Activity 生命周期
            if (mLifecycle != null) {
                mLifecycle!!.register()
            }

            // 回调监听
            if (mListener != null) {
                mListener!!.onShow(this)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
        }
        return this
    }

    /**
     * 取消
     */
    fun cancel(): CJToast {
        if (isShow) {
            try {

                // 反注册 Activity 生命周期
                if (mLifecycle != null) {
                    mLifecycle!!.unregister()
                }

                // 如果当前 WindowManager 没有附加这个 View 则会抛出异常
                // java.lang.IllegalArgumentException: View=android.widget.TextView{3d2cee7 V.ED..... ........ 0,0-312,153} not attached to window manager
                windowManager!!.removeView(view)
                // 回调监听
                if (mListener != null) {
                    mListener!!.onDismiss(this)
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            // 当前没有显示
            isShow = false
        }
        return this
    }

    /**
     * 更新
     */
    fun update() {
        // 更新 WindowManger 的显示
        windowManager!!.updateViewLayout(view, windowParams)
    }

    /**
     * 回收
     */
    fun recycle() {
        context = null
        windowManager = null
        mListener = null
        mDraggable = null
    }

    /**
     * 根据 ViewId 获取 View
     */
    fun <V : View?> findViewById(id: Int): V {
        checkNotNull(view) { "Please setup view" }
        return view!!.findViewById<View>(id) as V
    }

    /**
     * 跳转 Activity
     */
    fun startActivity(clazz: Class<out Activity?>?) {
        startActivity(Intent(context, clazz))
    }

    fun startActivity(intent: Intent) {
        if (context !is Activity) {
            // 如果当前的上下文不是 Activity，调用 startActivity 必须加入新任务栈的标记
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context!!.startActivity(intent)
    }

    /**
     * 设置可见状态
     */
    fun setVisibility(id: Int, visibility: Int): CJToast {
        findViewById<View>(id).visibility = visibility
        return this
    }

    /**
     * 设置文本
     */
    fun setText(id: Int): CJToast {
        return setText(R.id.message, id)
    }

    fun setText(viewId: Int, stringId: Int): CJToast {
        return setText(viewId, context!!.resources.getString(stringId))
    }

    fun setText(text: CharSequence?): CJToast {
        return setText(R.id.message, text)
    }

    fun setText(id: Int, text: CharSequence?): CJToast {
        (findViewById<View>(id) as TextView).text = text
        return this
    }

    /**
     * 设置文本颜色
     */
    fun setTextColor(id: Int, color: Int): CJToast {
        (findViewById<View>(id) as TextView).setTextColor(color)
        return this
    }

    /**
     * 设置提示
     */
    fun setHint(viewId: Int, stringId: Int): CJToast {
        return setHint(viewId, context!!.resources.getString(stringId))
    }

    fun setHint(id: Int, text: CharSequence?): CJToast {
        (findViewById<View>(id) as TextView).hint = text
        return this
    }

    /**
     * 设置提示文本颜色
     */
    fun setHintColor(id: Int, color: Int): CJToast {
        (findViewById<View>(id) as TextView).setHintTextColor(color)
        return this
    }

    /**
     * 设置背景
     */
    fun setBackground(viewId: Int, drawableId: Int): CJToast {
        val drawable: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context!!.getDrawable(drawableId)
        } else {
            context!!.resources.getDrawable(drawableId)
        }
        return setBackground(viewId, drawable)
    }

    fun setBackground(id: Int, drawable: Drawable?): CJToast {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findViewById<View>(id).background = drawable
        } else {
            findViewById<View>(id).setBackgroundDrawable(drawable)
        }
        return this
    }

    /**
     * 设置图片
     */
    fun setImageDrawable(viewId: Int, drawableId: Int): CJToast {
        val drawable: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context!!.getDrawable(drawableId)
        } else {
            context!!.resources.getDrawable(drawableId)
        }
        return setImageDrawable(viewId, drawable)
    }

    fun setImageDrawable(viewId: Int, drawable: Drawable?): CJToast {
        (findViewById<View>(viewId) as ImageView).setImageDrawable(drawable)
        return this
    }

    /**
     * 设置点击事件
     */
    fun setOnClickListener(listener: OnClickListener<*>): CJToast {
        return setOnClickListener(view, listener)
    }

    fun setOnClickListener(id: Int, listener: OnClickListener<*>): CJToast {
        return setOnClickListener(findViewById<View>(id), listener)
    }

    private fun setOnClickListener(view: View?, listener: OnClickListener<*>): CJToast {
        // 当前是否设置了不可触摸，如果是就擦除掉
        if (hasWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)) {
            removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        ViewClickWrapper(this, view!!, listener as OnClickListener<Any>)
        return this
    }

    /**
     * 设置触摸事件
     */
    fun setOnTouchListener(listener: OnTouchListener<*>): CJToast {
        return setOnTouchListener(view, listener)
    }

    fun setOnTouchListener(id: Int, listener: OnTouchListener<*>): CJToast {
        return setOnTouchListener(findViewById<View>(id), listener)
    }

    private fun setOnTouchListener(view: View?, listener: OnTouchListener<*>): CJToast {
        // 当前是否设置了不可触摸，如果是就擦除掉
        if (hasWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)) {
            removeWindowFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        ViewTouchWrapper(this, view!!, listener as OnTouchListener<Any>)
        return this
    }

    /**
     * 延迟执行
     */
    fun post(r: Runnable?): Boolean {
        return postDelayed(r, 0)
    }

    /**
     * 延迟一段时间执行
     */
    fun postDelayed(r: Runnable?, delayMillis: Long): Boolean {
        var delayMillis = delayMillis
        if (delayMillis < 0) {
            delayMillis = 0
        }
        return postAtTime(r, SystemClock.uptimeMillis() + delayMillis)
    }

    /**
     * 在指定的时间执行
     */
    fun postAtTime(r: Runnable?, uptimeMillis: Long): Boolean {
        // 发送和这个 WindowManager 相关的消息回调
        return handler.postAtTime(r!!, this, uptimeMillis)
    }

    /**
     * 移除消息回调
     */
    fun removeCallbacks() {
        handler.removeCallbacksAndMessages(this)
    }

    companion object {
        /**
         * 获取 Handler
         */
        val handler = Handler(Looper.getMainLooper())
    }

    init {
        this.context = context
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // 配置一些默认的参数
        windowParams = WindowManager.LayoutParams()
        windowParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        windowParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        windowParams!!.format = PixelFormat.TRANSLUCENT
        windowParams!!.windowAnimations = R.style.Animation_Toast
        windowParams!!.packageName = context.packageName
        // 开启窗口常亮和设置可以触摸外层布局（除 WindowManager 外的布局，默认是 WindowManager 显示的时候外层不可触摸）
        // 需要注意的是设置了 FLAG_NOT_TOUCH_MODAL 必须要设置 FLAG_NOT_FOCUSABLE，否则就会导致用户按返回键无效
        windowParams!!.flags = (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }
}