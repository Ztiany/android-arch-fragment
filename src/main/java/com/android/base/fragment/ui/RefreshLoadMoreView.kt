package com.android.base.fragment.ui

/**
 * An interface for a view that supports refresh and load more.
 *
 * @author Ztiany
 */
interface RefreshLoadMoreView {

    fun autoRefresh()

    fun refreshCompleted()

    fun isRefreshing(): Boolean

    fun setRefreshing()

    fun isLoadingMore(): Boolean

    fun setLoadingMore()

    /**
     * @param hasMore true if has more data.
     * @param appended true if there is data appended from last load more.
     */
    fun loadMoreCompleted(hasMore: Boolean, appended: Boolean)

    fun loadMoreFailed()

    fun setRefreshHandler(refreshHandler: RefreshHandler?)

    fun setLoadMoreHandler(loadMoreHandler: LoadMoreHandler?)

    var isRefreshEnable: Boolean

    var isLoadMoreEnable: Boolean

    interface RefreshHandler {
        fun onRefresh()
    }

    interface LoadMoreHandler {
        fun onLoadMore()
    }

}