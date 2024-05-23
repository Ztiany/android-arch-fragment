package com.android.base.fragment.list.paging3

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import com.android.base.core.AndroidSword
import com.android.base.fragment.tool.runRepeatedlyOnViewLifecycle
import com.android.base.fragment.ui.PagingHost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

fun <H, T : Any> H.handlePagingDataWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    adapter: PagingDataAdapter<T, *>,
    data: Flow<PagingData<T>>,
) where H : PagingHost, H : Fragment {
    runRepeatedlyOnViewLifecycle(activeState) {
        launch {
            data.collectLatest {
                adapter.submitData(it)
            }
        }
        launch {
            adapter.loadStateFlow.collectLatest {
                handleLoadState(adapter, it)
            }
        }
    }
}

private fun PagingHost.handleLoadState(adapter: PagingDataAdapter<*, *>, loadStates: CombinedLoadStates) {
    val isEmpty = adapter.snapshot().isEmpty()
    handlePagingRefreshState(loadStates.refresh, isEmpty)
    Timber.d("loadStates append: ${loadStates.append}")
    Timber.d("loadStates prepend: ${loadStates.prepend}")
    Timber.d("loadStates mediator: ${loadStates.mediator}")
    Timber.d("loadStates source: ${loadStates.source}")
    Timber.d("loadStates refresh: ${loadStates.refresh}")
    Timber.d("loadStates hasError: ${loadStates.hasError}")
    Timber.d("loadStates isIdle: ${loadStates.isIdle}")
}

private fun PagingHost.handlePagingRefreshState(
    refreshState: LoadState,
    isEmpty: Boolean,
    showContentLoadingWhenEmpty: Boolean = true,
    customErrorHandler: Boolean = false,
    customEmptyHandler: Boolean = false,
) {
    when (refreshState) {
        is LoadState.Loading -> {
            if (isEmpty && showContentLoadingWhenEmpty && !isRefreshing()) {
                showLoadingLayout()
            } else {
                setRefreshing()
            }
        }

        is LoadState.Error -> {
            // finished with error
            val refreshError = refreshState.error
            if (!customErrorHandler) {
                if (isEmpty) {
                    val errorTypeClassifier = AndroidSword.errorClassifier
                    if (errorTypeClassifier != null) {
                        when {
                            errorTypeClassifier.isNetworkError(refreshError) -> showNetErrorLayout()
                            errorTypeClassifier.isServerError(refreshError) -> showServerErrorLayout()
                            else -> showErrorLayout()
                        }
                    } else {
                        showErrorLayout()
                    }
                } else {
                    showContentLayout()
                }
            }
            //listStateHandler.onError?.invoke(true, refreshError)
            return
        }

        is LoadState.NotLoading -> {
            // finished with no error
            if (isEmpty && !isRefreshing()) {
                if (!customEmptyHandler) {
                    showEmptyLayout()
                }
                //listStateHandler.onEmpty?.invoke()
            } else {
                showContentLayout()
            }
            if (isRefreshing()) {
                refreshCompleted()
            }
        }
    }
}