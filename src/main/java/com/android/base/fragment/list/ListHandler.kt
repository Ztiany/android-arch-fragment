package com.android.base.fragment.list

import com.android.base.core.AndroidSword
import com.android.base.fragment.list.epoxy.BaseEpoxyListFragment
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

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

    internal var onRefreshEmpty: (suspend HandlingProcedure.() -> Unit)? = null
    internal var onRefreshCompleted: (suspend HandlingProcedure.() -> Unit)? = null
    internal var onRefreshError: (suspend HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit)? = null

    internal var onLoadMoreError: (suspend (error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: (suspend (reachedEnd: Boolean) -> Unit)? = null
    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun onOnRefreshResultEmpty(action: suspend HandlingProcedure.() -> Unit) {
        onRefreshEmpty = action
    }

    fun onOnRefreshCompleted(action: suspend HandlingProcedure.() -> Unit) {
        onRefreshCompleted = action
    }

    fun onRefreshError(action: suspend HandlingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit) {
        onRefreshError = action
    }

    fun onLoadMoreError(action: suspend (error: Throwable) -> Unit) {
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
            .collectLatest {
                Timber.d(it.toString())
                submitData(it)
            }
    }
    // handling refresh state
    launch {
        data.map { Triple(it.isRefreshing, it.refreshError, it.data.isEmpty()) }
            .distinctUntilChanged()
            .collectLatest {
                handleRefreshState(it, listHandler)
            }
    }
    // handling load more state
    launch {
        data.map { Triple(it.isLoadingMore, it.hasMore, it.loadMoreError) }
            .distinctUntilChanged()
            .collectLatest {
                handleLoadingMoreState(it, listHandler)
            }
    }
}

private suspend fun <T> ListLayoutHost<T>.handleRefreshState(
    refreshState: Triple<Boolean/*is refreshing*/, Throwable?, Boolean/* is empty*/>,
    listStateHandler: ListStateHandlerBuilder,
) {
    // refreshing
    if (refreshState.first) {
        if (refreshState.third /* isEmpty */ && listStateHandler.showContentLoadingWhenEmpty && !isRefreshing()) {
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
            if (refreshState.third /* isEmpty */) {
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
            HandlingProcedure(defaultHandling).it(refreshState.third /* isEmpty */, refreshError)
        } ?: defaultHandling()
        return
    }

    // refreshing
    if (refreshState.first) {
        return
    }

    // finished with no error
    if (refreshState.third /* isEmpty */ && !isRefreshing()) {
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

private suspend fun <T> ListLayoutHost<T>.handleLoadingMoreState(
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