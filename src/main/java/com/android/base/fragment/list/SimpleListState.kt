package com.android.base.fragment.list

import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.Paging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** A class used to model the state of a list. */
data class SimpleListState<T>(
    override val data: List<T> = emptyList(),
    override val isRefreshing: Boolean = false,
    override val refreshError: Throwable? = null,
    override val isLoadingMore: Boolean = false,
    override val loadMoreError: Throwable? = null,
    override val hasMore: Boolean = false,
) : ListState<T, SimpleListState<T>> {

    override fun toRefreshing(): SimpleListState<T> {
        return copy(isRefreshing = true, refreshError = null, isLoadingMore = false, loadMoreError = null)
    }

    override fun toLoadingMore(): SimpleListState<T> {
        return copy(isRefreshing = false, refreshError = null, isLoadingMore = true, loadMoreError = null)
    }

    override fun replaceList(list: List<T>, hasMore: Boolean): SimpleListState<T> {
        return copy(data = list, isRefreshing = false, isLoadingMore = false, hasMore = hasMore)
    }

    override fun toRefreshError(refreshError: Throwable): SimpleListState<T> {
        return copy(isRefreshing = false, refreshError = refreshError)
    }

    override fun appendList(list: List<T>, hasMore: Boolean): SimpleListState<T> {
        val oldList = data.toMutableList()
        oldList.addAll(list)
        return copy(data = oldList, isLoadingMore = false, hasMore = hasMore)
    }

    override fun toLoadMoreError(loadMoreError: Throwable): SimpleListState<T> {
        return copy(isLoadingMore = false, loadMoreError = loadMoreError)
    }

}

/** A builder for [ListStateHelper] working with [SimpleListState]. */
@Suppress("FunctionName")
fun <T> SimpleListStateHelper(
    state: MutableStateFlow<SimpleListState<T>> = MutableStateFlow(SimpleListState()),
    @HasMoreCheckMode checkMode: Int = HasMoreCheckMode.BY_PAGE_SIZE,
    paging: Paging = AutoPaging(initialSize = state.value.data.size),
): ListStateHelper<T, SimpleListState<T>> {
    return ListStateHelper(state, checkMode, paging)
}


/** A class used to multiple the state of a list. */
class ListStateHelper<T, LS : ListState<T, LS>>(
    val state: MutableStateFlow<LS>,
    @HasMoreCheckMode val checkMode: Int = HasMoreCheckMode.BY_PAGE_SIZE,
    val paging: Paging = AutoPaging(initialSize = state.value.data.size),
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