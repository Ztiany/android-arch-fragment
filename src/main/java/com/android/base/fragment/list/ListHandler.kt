package com.android.base.fragment.list

import com.android.base.core.AndroidSword
import com.android.base.fragment.list.epoxy.BaseEpoxyListFragment
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface ListState<T, LS : ListState<T, LS>> {

    val data: List<T>

    val isRefreshing: Boolean
    val refreshError: Throwable?

    val isLoadingMore: Boolean
    val loadMoreError: Throwable?
    val hasMore: Boolean

    fun toRefreshing(): LS

    fun toLoadingMore(): LS

    fun toRefreshError(refreshError: Throwable): LS

    fun toLoadMoreError(loadMoreError: Throwable): LS

    fun replaceList(list: List<T>, hasMore: Boolean): LS

    fun appendList(list: List<T>, hasMore: Boolean): LS

}

class ListStateHandlerBuilder internal constructor() {

    internal var onRefreshEmpty: (HandlingProcedure.() -> Unit)? = null
    internal var onRefreshCompleted: (HandlingProcedure.() -> Unit)? = null
    internal var onRefreshError: (HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit)? = null

    internal var onLoadMoreError: ((error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: ((reachedEnd: Boolean) -> Unit)? = null
    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun onOnRefreshResultEmpty(action: HandlingProcedure.() -> Unit) {
        onRefreshEmpty = action
    }

    fun onOnRefreshCompleted(action: HandlingProcedure.() -> Unit) {
        onRefreshCompleted = action
    }

    fun onRefreshError(action: HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit) {
        onRefreshError = action
    }

    fun onLoadMoreError(action: (error: Throwable) -> Unit) {
        onLoadMoreError = action
    }

    fun showContentLoadingWhenEmpty(enable: Boolean) {
        showContentLoadingWhenEmpty = enable
    }
}

/**
 * @see BaseEpoxyListFragment
 */
context(CoroutineScope)
fun <T> ListLayoutHost<T>.handleListState(data: Flow<ListState<T, *>>) {
    handleListState(data) {}
}

/**
 * @see BaseEpoxyListFragment
 */
context(CoroutineScope)
fun <T> ListLayoutHost<T>.handleListState(
    data: Flow<ListState<T, *>>,
    handlerBuilder: ListStateHandlerBuilder.() -> Unit,
) {

    val listHandler = ListStateHandlerBuilder().apply(handlerBuilder)

    // handling data
    launch {
        data.map { it.data }
            .distinctUntilChanged()
            .collect {
                submitData(it)
            }
    }
    // handling refresh state
    launch {
        data.map { Pair(it.isRefreshing, it.refreshError) }
            .distinctUntilChanged()
            .collect {
                handleRefreshState(it, listHandler)
            }
    }
    // handling load more state
    launch {
        data.map { Triple(it.isLoadingMore, it.hasMore, it.loadMoreError) }
            .distinctUntilChanged()
            .collect {
                handleLoadingMoreState(it, listHandler)
            }
    }
}

private fun <T> ListLayoutHost<T>.handleRefreshState(
    refreshState: Pair<Boolean/*is refreshing*/, Throwable?>,
    listStateHandler: ListStateHandlerBuilder,
) {
    // refreshing
    if (refreshState.first) {
        if (isEmpty() && listStateHandler.showContentLoadingWhenEmpty && !isRefreshing()) {
            showLoadingLayout()
        } else {
            setRefreshing()
        }
    } else {
        refreshCompleted()
    }

    // finished with an error
    val refreshError = refreshState.second
    if (refreshError != null) {
        // default handling process
        val defaultHandling = {
            if (isEmpty()) {
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
        listStateHandler.onRefreshError?.also {
            HandlingProcedure(defaultHandling).it(isEmpty(), refreshError)
        } ?: defaultHandling()
        return
    }

    // refreshing
    if (refreshState.first) {
        return
    }

    // finished with no error
    if (isEmpty() && !isRefreshing()) {
        // default handling process
        val defaultHandling = { showEmptyLayout() }
        // your custom handling process
        listStateHandler.onRefreshEmpty?.also {
            HandlingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    } else {
        // default handling process
        val defaultHandling = { showContentLayout() }
        // your custom handling process
        listStateHandler.onRefreshCompleted?.also {
            HandlingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    }
}

private fun <T> ListLayoutHost<T>.handleLoadingMoreState(
    /* Triple<LoadingMore, hasMore, loadMoreError> */
    loadMoreState: Triple<Boolean, Boolean, Throwable?>,
    listHandler: ListStateHandlerBuilder,
) {
    val loadMoreError = loadMoreState.third
    if (loadMoreState.first) {
        setLoadingMore()
        return
    }
    if (loadMoreError != null) {
        loadMoreFailed()
        listHandler.onLoadMoreError?.invoke(loadMoreError)
    } else {
        loadMoreCompleted(loadMoreState.second)
        listHandler.onLoadMoreCompleted?.invoke(!loadMoreState.second)
    }
}