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


internal class ListTaskHolder(private val defaultKey: String) {

    private var defaultJob: Job? = null

    private val jobs: MutableMap<String, Job> by lazy { ConcurrentHashMap() }

    fun setJob(key: String, job: Job) {
        if (key == defaultKey) {
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


private val vmListTasksHolder = ConcurrentHashMap<ViewModel, ListTaskHolder>()

fun ViewModel.startListJob(
    key: String = "default_vm_list_job_key",
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) {
    val listTaskHolder = vmListTasksHolder.getOrPut(this) {
        ListTaskHolder(key).apply {
            Timber.d("add the ListTaskHolder to ${this@ViewModel.javaClass.name}(${this@ViewModel.hashCode()}).")
            addCloseable {
                Timber.d("remove the ListTaskHolder from ${this@ViewModel.javaClass.name}(${this@ViewModel.hashCode()}).")
                vmListTasksHolder.remove(this@startListJob)
            }
        }
    }

    val job = viewModelScope.launch(context, start, block)
    listTaskHolder.setJob(key, job)
    Timber.d("add a list job to ${this.javaClass.name}(${this.hashCode()})'s ListTaskHolder.")

    job.invokeOnCompletion {
        Timber.d("a list job is completed in ${this.javaClass.name}(${this.hashCode()}) then remove it from it's ListTaskHolder.")
        listTaskHolder.removeJob(key)
    }
}