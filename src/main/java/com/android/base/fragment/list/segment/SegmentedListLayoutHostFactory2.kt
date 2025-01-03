package com.android.base.fragment.list.segment

import android.view.View
import com.android.base.fragment.ui.OnRetryActionListener
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.RefreshLoadMoreView
import com.android.base.fragment.ui.RefreshLoadMoreViewFactory
import com.android.base.fragment.ui.SegmentedListDataHost
import com.android.base.fragment.ui.SegmentedListLayoutHost
import com.android.base.fragment.ui.StateLayout
import com.android.base.fragment.ui.StateLayoutConfig

class SegmentedListLayoutHostConfig2<PageKey : Any> internal constructor() {
    var onRetry: ((state: Int) -> Unit)? = null
    var onRefresh: (() -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null
}

/** It is useful when there is more than one list layout in a fragment. */
fun <T, Key : Any> buildSegmentedListLayoutHost2(
    dataManager: SegmentedListDataHost<T>,
    paging: Paging<Key>,
    stateLayout: View,
    refreshLoadMoreView: View,
    config: SegmentedListLayoutHostConfig2<Key>.() -> Unit,
): SegmentedListLayoutHost<T, Key> {

    val stateLayoutImpl = (stateLayout as? StateLayout) ?: throw IllegalStateException("Make sure that stateLayout implements StateLayout.")
    val refreshLoadMoreViewImpl = RefreshLoadMoreViewFactory.createRefreshLoadMoreView(refreshLoadMoreView)

    val hostConfig = SegmentedListLayoutHostConfig2<Key>().apply(config)

    refreshLoadMoreViewImpl.setRefreshHandler(object : RefreshLoadMoreView.RefreshHandler {
        override fun onRefresh() {
            hostConfig.onRefresh?.invoke()
        }
    })

    refreshLoadMoreViewImpl.setLoadMoreHandler(object : RefreshLoadMoreView.LoadMoreHandler {
        override fun onLoadMore() {
            hostConfig.onLoadMore?.invoke()
        }
    })

    stateLayoutImpl.stateLayoutConfig.setStateRetryListener(object : OnRetryActionListener {
        override fun onRetry(state: Int) {
            hostConfig.onRetry?.invoke(state)
        }
    })

    return object : SegmentedListLayoutHost<T, Key> {

        override val paging: Paging<Key> = paging

        override fun replaceData(data: List<T>) {
            dataManager.replaceData(data)
        }

        override fun addData(data: List<T>) {
            dataManager.addData(data)
        }

        override fun loadMoreCompleted(hasMore: Boolean, appended: Boolean) {
            refreshLoadMoreViewImpl.loadMoreCompleted(hasMore, appended)
        }

        override fun loadMoreFailed() {
            refreshLoadMoreViewImpl.loadMoreFailed()
        }

        override fun isEmpty(): Boolean {
            return dataManager.isEmpty()
        }

        override fun getListSize(): Int {
            return dataManager.getListSize()
        }

        override fun isLoadingMore(): Boolean {
            return refreshLoadMoreViewImpl.isLoadingMore()
        }

        override fun setLoadingMore() {
            refreshLoadMoreViewImpl.setLoadingMore()
        }

        override fun isRefreshing(): Boolean {
            return refreshLoadMoreViewImpl.isRefreshing()
        }

        override fun setRefreshing() {
            refreshLoadMoreViewImpl.setRefreshing()
        }

        override var isLoadMoreEnable: Boolean
            get() = refreshLoadMoreViewImpl.isLoadMoreEnable
            set(value) {
                refreshLoadMoreViewImpl.isLoadMoreEnable = value
            }

        override fun autoRefresh() {
            refreshLoadMoreViewImpl.autoRefresh()
        }

        override fun refreshCompleted() {
            refreshLoadMoreViewImpl.refreshCompleted()
        }

        override var isRefreshEnable: Boolean
            get() = refreshLoadMoreViewImpl.isRefreshEnable
            set(value) {
                refreshLoadMoreViewImpl.isRefreshEnable = value
            }

        override fun showContentLayout() {
            stateLayoutImpl.showContentLayout()
        }

        override fun showLoadingLayout() {
            stateLayoutImpl.showLoadingLayout()
        }

        override fun showEmptyLayout() {
            stateLayoutImpl.showEmptyLayout()
        }

        override fun showErrorLayout() {
            stateLayoutImpl.showErrorLayout()
        }

        override fun showRequesting() {
            stateLayoutImpl.showRequesting()
        }

        override fun showBlank() {
            stateLayoutImpl.showBlank()
        }

        override fun showNetErrorLayout() {
            stateLayoutImpl.showNetErrorLayout()
        }

        override fun showServerErrorLayout() {
            stateLayoutImpl.showServerErrorLayout()
        }

        override fun getStateLayoutConfig(): StateLayoutConfig {
            return stateLayoutImpl.stateLayoutConfig
        }

        override fun currentStatus(): Int {
            return stateLayoutImpl.currentStatus()
        }

    }

}