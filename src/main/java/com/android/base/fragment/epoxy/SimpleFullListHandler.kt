package com.android.base.fragment.epoxy

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.android.base.arch.mvi.UIState
import com.android.base.core.AndroidSword
import com.android.base.fragment.tool.runRepeatedlyOnViewLifecycle
import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A class used to model the state of a list. */
data class ListState<T>(
    /** 列表数据 */
    val data: List<T> = emptyList(),

    // 刷新状态
    val isRefreshing: Boolean = false,
    val refreshError: Throwable? = null,

    // 加载更多状态
    val isLoadingMore: Boolean = false,
    val loadMoreError: Throwable? = null,
    val hasMore: Boolean = false,
) : UIState

fun <T> ListState<T>.toRefreshing(): ListState<T> {
    return copy(isRefreshing = true, refreshError = null, isLoadingMore = false, loadMoreError = null)
}

fun <T> ListState<T>.toLoadingMore(): ListState<T> {
    return copy(isRefreshing = false, refreshError = null, isLoadingMore = true, loadMoreError = null)
}

fun <T> ListState<T>.toRefreshError(refreshError: Throwable): ListState<T> {
    return copy(isRefreshing = false, refreshError = refreshError)
}

fun <T> ListState<T>.toLoadMoreError(loadMoreError: Throwable): ListState<T> {
    return copy(isLoadingMore = false, loadMoreError = loadMoreError)
}

fun <T> ListState<T>.replaceList(list: List<T>, hasMore: Boolean): ListState<T> {
    return copy(data = list, isRefreshing = false, isLoadingMore = false, hasMore = hasMore)
}

fun <T> ListState<T>.appendList(list: List<T>, hasMore: Boolean): ListState<T> {
    val oldList = data.toMutableList()
    oldList.addAll(list)
    return copy(data = oldList, isLoadingMore = false, hasMore = hasMore)
}

/** A class used to multiple the state of a list. */
class ListStateHelper<T>(
    val state: MutableStateFlow<ListState<T>> = MutableStateFlow(ListState()),
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

class ListStateHandlerBuilder {
    internal var onEmpty: (() -> Unit)? = null
    internal var onError: ((isRefresh: Boolean, error: Throwable) -> Unit)? = null
    internal var showContentLoadingWhenEmpty = false
    internal var customErrorHandler = false
    internal var customEmptyHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        customEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (isRefresh: Boolean, error: Throwable) -> Unit) {
        onError = action
        customErrorHandler = monopolized
    }

    fun useContentLoadingWhenEmpty() {
        showContentLoadingWhenEmpty = true
    }
}

fun <H, T> H.handleListStateWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<ListState<T>>,
) where H : ListLayoutHost<T>, H : Fragment {
    handleListStateWithViewLifecycle(activeState, data) {
        // nothing to do.
    }
}

fun <H, T> H.handleListStateWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<ListState<T>>,
    handlerBuilder: ListStateHandlerBuilder.() -> Unit,
) where H : ListLayoutHost<T>, H : Fragment {

    val listHandler = ListStateHandlerBuilder().apply(handlerBuilder)

    runRepeatedlyOnViewLifecycle(activeState) {
        // 数据
        launch {
            data.map { it.data }
                .distinctUntilChanged()
                .collect {
                    replaceData(it)
                }
        }
        // 刷新状态
        launch {
            data.map { Pair(it.isRefreshing, it.refreshError) }
                .distinctUntilChanged()
                .collect {
                    handleRefreshState(it, listHandler)
                }
        }
        // 加载更多状态
        launch {
            data.map { Triple(it.isLoadingMore, it.hasMore, it.loadMoreError) }
                .distinctUntilChanged()
                .collect {
                    handleLoadingMoreState(it, listHandler)
                }
        }
    }
}

private fun <T> ListLayoutHost<T>.handleRefreshState(refreshState: Pair<Boolean, Throwable?>, listStateHandler: ListStateHandlerBuilder) {
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

    // finished with error
    val refreshError = refreshState.second
    if (refreshError != null) {
        if (!listStateHandler.customErrorHandler) {
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
        listStateHandler.onError?.invoke(true, refreshError)
        return
    }

    if (refreshState.first) {
        return
    }

    // finished with no error
    if (isEmpty() && !isRefreshing()) {
        if (!listStateHandler.customEmptyHandler) {
            showEmptyLayout()
        }
        listStateHandler.onEmpty?.invoke()
    } else {
        showContentLayout()
    }
}

private fun <T> ListLayoutHost<T>.handleLoadingMoreState(loadMoreState: Triple<Boolean, Boolean, Throwable?>, listHandler: ListStateHandlerBuilder) {
    val loadMoreError = loadMoreState.third
    if (loadMoreState.first) {
        setLoadingMore()
        return
    }
    if (loadMoreError != null) {
        loadMoreFailed()
        listHandler.onError?.invoke(false, loadMoreError)
    } else {
        loadMoreCompleted(loadMoreState.second)
    }
}