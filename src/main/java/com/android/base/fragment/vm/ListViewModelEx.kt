package com.android.base.fragment.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private const val DEFAULT_KEY = "default_list_job_key"

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
}

private val refreshTasks = ConcurrentHashMap<ViewModel, RefreshTask>()

fun ViewModel.startListJob(
    key: String = DEFAULT_KEY,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) {
    val refreshTask = refreshTasks.getOrPut(this) {
        RefreshTask().apply {
            Timber.d("add refresh task in ViewModel: $this")
            addCloseable {
                Timber.d("remove refresh task in ViewModel: $this")
                refreshTasks.remove(this@startListJob)
            }
        }
    }

    val job = viewModelScope.launch(context, start, block)
    refreshTask.setJob(key, job)
}
