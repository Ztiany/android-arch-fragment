package com.android.base.fragment.list.paging3

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import com.android.base.core.AndroidSword
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.PagingLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PagingDataHandlerBuilder<T : Any> internal constructor() {

    internal var onRefreshError: (suspend HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit)? = null
    internal var onRefreshEmpty: (suspend HandlingProcedure.() -> Unit)? = null
    internal var onRefreshCompleted: (suspend HandlingProcedure.() -> Unit)? = null

    internal var onLoadMoreError: (suspend (error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: (suspend (reachedEnd: Boolean) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    /** handle when the list is empty after a successful refreshing. */
    fun onRefreshEmpty(action: suspend HandlingProcedure.() -> Unit) {
        onRefreshEmpty = action
    }

    fun onRefreshCompleted(action: suspend HandlingProcedure. () -> Unit) {
        onRefreshCompleted = action
    }

    fun onRefreshError(action: suspend HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit) {
        onRefreshError = action
    }

    fun onLoadMoreError(action: suspend (error: Throwable) -> Unit) {
        onLoadMoreError = action
    }

    fun onLoadMoreCompleted(action: suspend (reachedEnd: Boolean) -> Unit) {
        onLoadMoreCompleted = action
    }

    fun showContentLoadingWhenEmpty(enable: Boolean) {
        showContentLoadingWhenEmpty = enable
    }
}

/**
 * This extension is used to help [BasePagingFragment] or [BasePagingDialogFragment] handle paging data.
 */
context(CoroutineScope)
fun <T : Any> PagingLayoutHost.handlePagingData(
    adapter: PagingDataAdapter<T, *>,
    data: Flow<PagingData<T>>,
) {
    handlePagingData(adapter, data) {}
}

/**
 * This extension is used to help [BasePagingFragment] or [BasePagingDialogFragment] handle paging data.
 */
context(CoroutineScope)
fun <T : Any> PagingLayoutHost.handlePagingData(
    adapter: PagingDataAdapter<T, *>,
    data: Flow<PagingData<T>>,
    handlerBuilder: PagingDataHandlerBuilder<T>.() -> Unit,
) {
    val pagingDataHandler = PagingDataHandlerBuilder<T>().apply(handlerBuilder)

    launch {
        data.collectLatest {
            adapter.submitData(it)
        }
    }

    launch {
        adapter.loadStateFlow.collectLatest {
            handleLoadState(adapter, it, pagingDataHandler)
        }
    }
}

private suspend fun PagingLayoutHost.handleLoadState(
    adapter: PagingDataAdapter<*, *>,
    loadStates: CombinedLoadStates,
    pagingDataHandler: PagingDataHandlerBuilder<*>,
) {
    val isEmpty = adapter.snapshot().isEmpty()
    handlePagingRefreshState(loadStates.refresh, isEmpty, pagingDataHandler)
    handlePagingLoadMoreState(loadStates.append, pagingDataHandler)
}

private suspend fun handlePagingLoadMoreState(append: LoadState, pagingDataHandler: PagingDataHandlerBuilder<*>) {
    when (append) {
        is LoadState.Loading -> {
            // do nothing
        }

        is LoadState.Error -> {
            pagingDataHandler.onLoadMoreError?.invoke(append.error)
        }

        is LoadState.NotLoading -> {
            // finished with no error
            pagingDataHandler.onLoadMoreCompleted?.invoke(append.endOfPaginationReached)
        }
    }
}

private suspend fun PagingLayoutHost.handlePagingRefreshState(
    refreshState: LoadState,
    isEmpty: Boolean,
    pagingDataHandler: PagingDataHandlerBuilder<*>,
) {
    when (refreshState) {
        is LoadState.Loading -> {
            if (isEmpty && pagingDataHandler.showContentLoadingWhenEmpty && !isRefreshing()) {
                showLoadingLayout()
            } else {
                setRefreshing()
            }
        }

        is LoadState.Error -> {
            // finished with an error
            val refreshError = refreshState.error
            // default handling process
            val defaultHandling = {
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
            // your custom handling process
            pagingDataHandler.onRefreshError?.also {
                HandlingProcedure(defaultHandling).it(true, refreshError)
            } ?: defaultHandling()
            return
        }

        is LoadState.NotLoading -> {
            // finished with no error
            if (isEmpty) {
                // default handling process
                val defaultHandling = { showEmptyLayout() }
                // your custom handling process
                pagingDataHandler.onRefreshEmpty?.also {
                    HandlingProcedure(defaultHandling).it()
                } ?: defaultHandling()
            } else {
                // default handling process
                val defaultHandling = { showContentLayout() }
                // your custom handling process
                pagingDataHandler.onRefreshCompleted?.also {
                    HandlingProcedure(defaultHandling).it()
                } ?: defaultHandling()
            }
            if (isRefreshing()) {
                refreshCompleted()
            }
        }
    }
}