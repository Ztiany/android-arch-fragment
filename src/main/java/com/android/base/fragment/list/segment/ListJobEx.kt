package com.android.base.fragment.list.segment

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.base.fragment.vm.ListTaskHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val fragmentListTasksHolder = ConcurrentHashMap<Fragment, ListTaskHolder>()

fun Fragment.startListJob(
    key: String = "default_fragment_list_job_key",
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) {

    val listTaskHolder = fragmentListTasksHolder.getOrPut(this) {
        ListTaskHolder(key).apply {
            Timber.d("add the ListTaskHolder to ${this@Fragment.javaClass.name}(${this@Fragment.hashCode()}).")
            lifecycle.addObserver(observer = object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    Timber.d("remove the ListTaskHolder from ${this@Fragment.javaClass.name}(${this@Fragment.hashCode()}).")
                    fragmentListTasksHolder.remove(this@startListJob)
                }
            })
        }
    }

    val job = lifecycleScope.launch(context, start, block)
    listTaskHolder.setJob(key, job)
    Timber.d("add a list job to ${this.javaClass.name}(${this.hashCode()})'s ListTaskHolder.")

    job.invokeOnCompletion {
        Timber.d("a list job is completed in ${this.javaClass.name}(${this.hashCode()}) then remove it from it's ListTaskHolder.")
        listTaskHolder.removeJob(key)
    }
}