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
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PagingDataHandlerBuilder<T : Any> internal constructor() {

    internal var onEmpty: (() -> Unit)? = null
    internal var monopolizedEmptyHandler = false

    internal var onRefreshError: ((isEmpty: Boolean, error: Throwable) -> Unit)? = null
    internal var monopolizedRefreshErrorHandler = false

    internal var onLoadMoreError: ((error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: ((reachedEnd: Boolean) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        monopolizedEmptyHandler = monopolized
    }

    fun onRefreshError(monopolized: Boolean = false, action: (isEmpty: Boolean, error: Throwable) -> Unit) {
        onRefreshError = action
        monopolizedRefreshErrorHandler = monopolized
    }

    fun onLoadMoreError(action: (error: Throwable) -> Unit) {
        onLoadMoreError = action
    }

    fun onLoadMoreCompleted(action: (reachedEnd: Boolean) -> Unit) {
        onLoadMoreCompleted = action
    }

    fun showContentLoadingWhenEmpty(enable: Boolean) {
        showContentLoadingWhenEmpty = enable
    }
}

/**
 * This extension is used to help [BasePagingFragment] or [BasePagingDialogFragment] handle paging data.
 */
fun <H, T : Any> H.handlePagingDataWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    adapter: PagingDataAdapter<T, *>,
    data: Flow<PagingData<T>>,
) where H : PagingHost, H : Fragment {
    handlePagingDataWithViewLifecycle(activeState, adapter, data) {
        // nothing to do.
    }
}

/**
 * This extension is used to help [BasePagingFragment] or [BasePagingDialogFragment] handle paging data.
 */
fun <H, T : Any> H.handlePagingDataWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    adapter: PagingDataAdapter<T, *>,
    data: Flow<PagingData<T>>,
    handlerBuilder: PagingDataHandlerBuilder<T>.() -> Unit,
) where H : PagingHost, H : Fragment {
    val pagingDataHandler = PagingDataHandlerBuilder<T>().apply(handlerBuilder)

    runRepeatedlyOnViewLifecycle(activeState) {
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
}

private fun PagingHost.handleLoadState(
    adapter: PagingDataAdapter<*, *>,
    loadStates: CombinedLoadStates,
    pagingDataHandler: PagingDataHandlerBuilder<*>,
) {
    val isEmpty = adapter.snapshot().isEmpty()
    handlePagingRefreshState(loadStates.refresh, isEmpty, pagingDataHandler)
    handlePagingLoadMoreState(loadStates.append, pagingDataHandler)
}

private fun handlePagingLoadMoreState(append: LoadState, pagingDataHandler: PagingDataHandlerBuilder<*>) {
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

private fun PagingHost.handlePagingRefreshState(
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
            // default handing process
            if (!pagingDataHandler.monopolizedRefreshErrorHandler) {
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
            // your custom handing process
            pagingDataHandler.onRefreshError?.invoke(true, refreshError)
            return
        }

        is LoadState.NotLoading -> {
            // finished with no error
            if (isEmpty && !isRefreshing()) {
                // default handing process
                if (!pagingDataHandler.monopolizedEmptyHandler) {
                    showEmptyLayout()
                }
                // your custom handing process
                pagingDataHandler.onEmpty?.invoke()
            } else {
                showContentLayout()
            }
            if (isRefreshing()) {
                refreshCompleted()
            }
        }
    }
}