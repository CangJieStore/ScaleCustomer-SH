package com.cangjie.scalage.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import java.io.Closeable

class CoroutineCycle() : Closeable {
    constructor(
            owner: LifecycleOwner,
            lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
    ) : this(owner.lifecycle, lifeEvent)

    constructor(
            lifecycle: Lifecycle,
            lifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
    ) : this() {
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (lifeEvent == event) {
                    close()
                    lifecycle.removeObserver(this)
                }
            }
        })
    }

    private val coroutineScope: CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return launch(block, null)
    }

    fun launch(
            block: suspend CoroutineScope.() -> Unit,
            onError: ((Throwable) -> Unit)? = null,
            onStart: (() -> Unit)? = null,
            onFinally: (() -> Unit)? = null
    ): Job {
        return coroutineScope.launch {
            try {
                coroutineScope {
                    onStart?.invoke()
                    block()
                }
            } catch (e: Throwable) {
                if (onError != null) {
                    onError(e)
                } else {
                    e.printStackTrace()
                }
            } finally {
                onFinally?.invoke()
            }
        }
    }

    override fun close() {
        coroutineScope.cancel()
    }
}