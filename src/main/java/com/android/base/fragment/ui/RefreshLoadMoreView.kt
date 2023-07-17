package com.android.base.fragment.ui

/**
 * 对下拉刷新/加载更多的抽象。
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

    fun loadMoreCompleted(hasMore: Boolean)

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