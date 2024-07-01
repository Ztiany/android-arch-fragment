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

/** A class used to work with [ListState]. */
class AutoPagingListStateHelper<T, LS : ListState<T, LS>>(
    val state: MutableStateFlow<LS>,
) {

    private val _paging = AutoPaging()
    val paging: Paging<Int>
        get() = _paging

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
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageRefreshed(list.size)
        state.update { it.replaceList(list, hasMore) }
    }

    fun appendListAndUpdate(list: List<T>, hasMore: Boolean) {
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageAppended(list.size)
        state.update { it.appendList(list, hasMore) }
    }

    fun replaceListAndUpdate(list: List<T>) {
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageRefreshed(list.size)
        state.update { it.replaceList(list, paging.hasMore(list.size)) }
    }

    fun appendListAndUpdate(list: List<T>) {
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageAppended(list.size)
        state.update { it.appendList(list, paging.hasMore(list.size)) }
    }

}

/** A builder for [AutoPagingListStateHelper] working with [SimpleListState]. */
@Suppress("FunctionName")
fun <T> SimpleListStateHelper(
    state: MutableStateFlow<SimpleListState<T>> = MutableStateFlow(SimpleListState()),
): AutoPagingListStateHelper<T, SimpleListState<T>> {
    return AutoPagingListStateHelper(state)
}