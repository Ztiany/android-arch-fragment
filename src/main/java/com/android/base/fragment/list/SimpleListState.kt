package com.android.base.fragment.list

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