package com.android.base.fragment.list

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.android.base.core.AndroidSword
import com.android.base.fragment.list.epoxy.BaseEpoxyListFragment
import com.android.base.fragment.tool.HandingProcedure
import com.android.base.fragment.tool.runRepeatedlyOnViewLifecycle
import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A builder for [ListStateHelper] working with [SimpleListState]. */
@Suppress("FunctionName")
fun <T> SimpleListStateHelper(
    state: MutableStateFlow<SimpleListState<T>> = MutableStateFlow(SimpleListState()),
    @HasMoreCheckMode checkMode: Int = HasMoreCheckMode.BY_PAGE_SIZE,
    /** callback for providing the list's size which will be used to calculate the page number. you don't need to provide it. it is only for debugging. */
    realListSize: (List<T>) -> Int = { it.size },
    paging: Paging = AutoPaging {
        realListSize(state.value.data)
    },
): ListStateHelper<T, SimpleListState<T>> {
    return ListStateHelper(state, checkMode, realListSize, paging)
}

/** A class used to multiple the state of a list. */
class ListStateHelper<T, LS : ListState<T, LS>>(
    val state: MutableStateFlow<LS>,
    @HasMoreCheckMode val checkMode: Int = HasMoreCheckMode.BY_PAGE_SIZE,
    /** callback for providing the list's size which will be used to calculate the page number. you don't need to provide it. it is only for debugging. */
    realListSize: (List<T>) -> Int = { it.size },
    val paging: Paging = AutoPaging {
        realListSize(state.value.data)
    },
) {

    fun updateToRefreshing() {
        state.update {
            it.toRefreshing()
        }
    }

    fun updateToLoadingMore() {
        state.update {
            it.toLoadingMore()
        }
    }

    fun updateToRefreshError(error: Throwable) {
        state.update {
            it.toRefreshError(error)
        }
    }

    fun updateToLoadMoreError(error: Throwable) {
        state.update {
            it.toLoadMoreError(error)
        }
    }

    fun replaceListAndUpdate(list: List<T>, hasMore: Boolean) {
        paging.onPageRefreshed(list.size)
        state.update { it.replaceList(list, hasMore) }
    }

    fun appendListAndUpdate(list: List<T>, hasMore: Boolean) {
        paging.onPageAppended(list.size)
        state.update { it.appendList(list, hasMore) }
    }

    fun replaceListAndUpdate(list: List<T>) {
        paging.onPageRefreshed(list.size)
        state.update { it.replaceList(list, hasMore(list)) }
    }

    fun appendListAndUpdate(list: List<T>) {
        paging.onPageAppended(list.size)
        state.update { it.appendList(list, hasMore(list)) }
    }

    private fun hasMore(list: List<T>): Boolean {
        return if (checkMode == HasMoreCheckMode.BY_PAGE_SIZE) {
            paging.hasMore(list.size)
        } else {
            list.isNotEmpty()
        }
    }

}

class ListStateHandlerBuilder internal constructor() {

    internal var onRefreshEmpty: (HandingProcedure.() -> Unit)? = null
    internal var onRefreshCompleted: (HandingProcedure.() -> Unit)? = null
    internal var onRefreshError: (HandingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit)? = null

    internal var onLoadMoreError: ((error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: ((reachedEnd: Boolean) -> Unit)? = null
    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun onOnRefreshResultEmpty(action: HandingProcedure.() -> Unit) {
        onRefreshEmpty = action
    }

    fun onOnRefreshCompleted(action: HandingProcedure.() -> Unit) {
        onRefreshCompleted = action
    }

    fun onRefreshError(action: HandingProcedure.(isEmpty: Boolean, error: Throwable) -> Unit) {
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
fun <H, T> H.handleListStateWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<ListState<T, *>>,
) where H : ListLayoutHost<T>, H : Fragment {
    handleListStateWithViewLifecycle(activeState, data) {
        // nothing to do.
    }
}

/**
 * @see BaseEpoxyListFragment
 */
fun <H, T> H.handleListStateWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<ListState<T, *>>,
    handlerBuilder: ListStateHandlerBuilder.() -> Unit,
) where H : ListLayoutHost<T>, H : Fragment {

    val listHandler = ListStateHandlerBuilder().apply(handlerBuilder)

    runRepeatedlyOnViewLifecycle(activeState) {
        // handling data
        launch {
            data.map { it.data }
                .distinctUntilChanged()
                .collect {
                    replaceData(it)
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
            HandingProcedure(defaultHandling).it(isEmpty(), refreshError)
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
            HandingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    } else {
        // default handling process
        val defaultHandling = { showContentLayout() }
        // your custom handling process
        listStateHandler.onRefreshCompleted?.also {
            HandingProcedure(defaultHandling).it()
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