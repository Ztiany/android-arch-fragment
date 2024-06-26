package com.android.base.fragment.list

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.android.base.core.AndroidSword
import com.android.base.fragment.list.epoxy.BaseEpoxyListFragment
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
    listSize: (List<T>) -> Int = { it.size },
    paging: Paging = AutoPaging {
        listSize(state.value.data)
    },
): ListStateHelper<T, SimpleListState<T>> {
    return ListStateHelper(state, listSize, paging)
}

/** A class used to multiple the state of a list. */
class ListStateHelper<T, LS : ListState<T, LS>>(
    val state: MutableStateFlow<LS>,
    listSize: (List<T>) -> Int = { it.size },
    val paging: Paging = AutoPaging {
        listSize(state.value.data)
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
        state.update { it.replaceList(list, hasMore) }
    }

    fun appendListAndUpdate(list: List<T>, hasMore: Boolean) {
        state.update { it.appendList(list, hasMore) }
    }

    fun replaceListAndUpdate(list: List<T>) {
        state.update { it.replaceList(list, paging.hasMore(list.size)) }
    }

    fun appendListAndUpdate(list: List<T>) {
        state.update { it.appendList(list, paging.hasMore(list.size)) }
    }

}

class ListStateHandlerBuilder internal constructor() {
    internal var onEmpty: (() -> Unit)? = null
    internal var onRefreshError: ((isEmpty: Boolean, error: Throwable) -> Unit)? = null
    internal var onLoadMoreError: ((error: Throwable) -> Unit)? = null
    internal var onLoadMoreCompleted: ((reachedEnd: Boolean) -> Unit)? = null
    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh
    internal var monopolizedErrorHandler = false
    internal var monopolizedEmptyHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        monopolizedEmptyHandler = monopolized
    }

    fun onRefreshError(monopolized: Boolean = false, action: (isEmpty: Boolean, error: Throwable) -> Unit) {
        onRefreshError = action
        monopolizedErrorHandler = monopolized
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
        // handing data
        launch {
            data.map { it.data }
                .distinctUntilChanged()
                .collect {
                    replaceData(it)
                }
        }
        // handing refresh state
        launch {
            data.map { Pair(it.isRefreshing, it.refreshError) }
                .distinctUntilChanged()
                .collect {
                    handleRefreshState(it, listHandler)
                }
        }
        // handing load more state
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
        // default handing process
        if (!listStateHandler.monopolizedErrorHandler) {
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
        // your custom handing process
        listStateHandler.onRefreshError?.invoke(isEmpty(), refreshError)
        return
    }

    if (refreshState.first) {
        return
    }

    // finished with no error
    if (isEmpty() && !isRefreshing()) {
        // default handing process
        if (!listStateHandler.monopolizedEmptyHandler) {
            showEmptyLayout()
        }
        // your custom handing process
        listStateHandler.onEmpty?.invoke()
    } else {
        showContentLayout()
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