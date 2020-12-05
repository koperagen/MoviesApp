package com.example.moviesapp.shared

import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.utils.internal.ensureNeverFrozen
import com.example.moviesapp.shared.cache.AppDatabase
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import platform.Foundation.NSRunLoop
import platform.Foundation.performBlock
import platform.darwin.*
import kotlin.coroutines.CoroutineContext

class IndexControllerFactory(
    private val appDatabase: AppDatabase,
    private val defaultStoreFactory: StoreFactory,
    private val lifecycle: Lifecycle
) {

    constructor(appDatabase: AppDatabase, lifecycle: Lifecycle) : this(
        appDatabase,
        LoggingStoreFactory(delegate = DefaultStoreFactory),
        lifecycle = lifecycle
    )

    init {
        ensureNeverFrozen()
    }

    fun create(): IndexController {
        return IndexController(
            dbFactory = { appDatabase },
            defaultStoreFactory = defaultStoreFactory,
            apiKey = "5e2154d0d7039ef73d73e64af47e8e6e",
            mainContext = MainLoopDispatcher,
            ioContext = MainLoopDispatcher,
            lifecycle = lifecycle
        )
    }
}


@OptIn(InternalCoroutinesApi::class)
private object MainLoopDispatcher: CoroutineDispatcher(), Delay {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatch_get_main_queue()) {
            try {
                block.run()
            } catch (err: Throwable) {
                //logError("UNCAUGHT", err.message ?: "", err)
                throw err
            }
        }
    }



    @InternalCoroutinesApi
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, timeMillis * 1_000_000), dispatch_get_main_queue()) {
            try {
                with(continuation) {
                    resumeUndispatched(Unit)
                }
            } catch (err: Throwable) {
                //logError("UNCAUGHT", err.message ?: "", err)
                throw err
            }
        }
    }

}

