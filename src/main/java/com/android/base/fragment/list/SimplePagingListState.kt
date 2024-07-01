package com.android.base.fragment.list

import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.Paging

/** An implementation of ListState that supports paging. */
data class AutoPagingListState<T>(
    override val data: List<T> = emptyList(),
    override val isRefreshing: Boolean = false,
    override val refreshError: Throwable? = null,
    override val isLoadingMore: Boolean = false,
    override val loadMoreError: Throwable? = null,
    override val hasMore: Boolean = false,
) : ListState<T, AutoPagingListState<T>> {

    private val _paging = AutoPaging()
    val paging: Paging<Int>
        get() = _paging

    override fun toRefreshing(): AutoPagingListState<T> {
        return copy(isRefreshing = true, refreshError = null, isLoadingMore = false, loadMoreError = null)
    }

    override fun toLoadingMore(): AutoPagingListState<T> {
        return copy(isRefreshing = false, refreshError = null, isLoadingMore = true, loadMoreError = null)
    }

    override fun replaceList(list: List<T>, hasMore: Boolean): AutoPagingListState<T> {
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageRefreshed(list.size)
        return copy(data = list, isRefreshing = false, isLoadingMore = false, hasMore = hasMore)
    }

    override fun toRefreshError(refreshError: Throwable): AutoPagingListState<T> {
        return copy(isRefreshing = false, refreshError = refreshError)
    }

    override fun appendList(list: List<T>, hasMore: Boolean): AutoPagingListState<T> {
        val oldList = data.toMutableList()
        oldList.addAll(list)
        /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
        paging.onPageAppended(list.size)
        return copy(data = oldList, isLoadingMore = false, hasMore = hasMore)
    }

    override fun toLoadMoreError(loadMoreError: Throwable): AutoPagingListState<T> {
        return copy(isLoadingMore = false, loadMoreError = loadMoreError)
    }

}