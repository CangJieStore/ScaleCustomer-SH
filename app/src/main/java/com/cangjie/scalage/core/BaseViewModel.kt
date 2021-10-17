package com.cangjie.scalage.core

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import com.cangjie.scalage.core.event.ActionEvent
import com.cangjie.scalage.core.event.MsgEvent
import com.blankj.utilcode.util.Utils

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/8/7 17:30
 */
open class BaseViewModel : AndroidViewModel(Utils.getApp()), LifecycleObserver {

    val defUI: UIEvent by lazy { UIEvent() }


    fun loading(word: String?) {
        defUI.showDialog.postValue(word)
    }

    fun dismissLoading() {
        defUI.dismissDialog.call()
    }

    fun toast(notice: String) {
        defUI.toastEvent.postValue(notice)
    }

    fun action(event: MsgEvent) {
        defUI.actionEvent.postValue(event)
    }


    inner class UIEvent {
        val showDialog by lazy { ActionEvent<String>() }
        val dismissDialog by lazy { ActionEvent<Void>() }
        val toastEvent by lazy { ActionEvent<String>() }
        val actionEvent by lazy { ActionEvent<MsgEvent>() }
    }
}