package com.android.base.fragment.list.segment

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private const val DEFAULT_KEY = "default_fragment_list_job_key"

private class RefreshTask {
    private var defaultJob: Job? = null
    private val jobs: MutableMap<String, Job> by lazy { ConcurrentHashMap() }

    fun setJob(key: String, job: Job) {
        if (key == DEFAULT_KEY) {
            defaultJob?.cancel()
            defaultJob = job
        } else {
            jobs.put(key, job)?.cancel()
        }
    }

    fun removeJob(key: String) {
        jobs.remove(key)
    }

}

private val fragmentRefreshTasks = ConcurrentHashMap<Fragment, RefreshTask>()

fun Fragment.startListJob(
    key: String = DEFAULT_KEY,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) {

    val refreshTask = fragmentRefreshTasks.getOrPut(this) {
        RefreshTask().apply {
            Timber.d("add refresh task in Fragment: ${this}.")
            lifecycle.addObserver(observer = object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    Timber.d("remove refresh task in Fragment: ${this}.")
                    fragmentRefreshTasks.remove(this@startListJob)
                }
            })
        }
    }

    val job = lifecycleScope.launch(context, start, block)
    refreshTask.setJob(key, job)

    job.invokeOnCompletion {
        Timber.d("job completed in Fragment: $this and remove it from tasks.")
        refreshTask.removeJob(key)
    }
}