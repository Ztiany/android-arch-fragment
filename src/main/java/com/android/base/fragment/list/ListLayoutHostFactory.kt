package com.android.base.fragment.list

import android.view.View
import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.ListDataHost
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.OnRetryActionListener
import com.android.base.fragment.ui.PagerSize
import com.android.base.fragment.ui.RefreshView
import com.android.base.fragment.ui.RefreshViewFactory
import com.android.base.fragment.ui.StateLayout
import com.android.base.fragment.ui.StateLayoutConfig
import com.ztiany.loadmore.adapter.LoadMoreController
import com.ztiany.loadmore.adapter.OnLoadMoreListener
import timber.log.Timber

class ListLayoutHostConfig {
    var onRetry: ((state: Int) -> Unit)? = null
    var onRefresh: (() -> Unit)? = null
    var onLoadMore: (() -> Unit)? = null
}

/** It is useful when there is more than one list layout in a fragment. */
fun <T> buildListLayoutHost(
    listDataHost: ListDataHost<T>,
    loadMoreController: LoadMoreController?,
    stateLayout: View,
    refreshLayout: View? = null,
    config: ListLayoutHostConfig.() -> Unit,
): ListLayoutHost<T> {

    val stateLayoutImpl = (stateLayout as? StateLayout) ?: throw IllegalStateException("Make sure that stateLayout implements StateLayout.")

    val refreshLayoutImpl = if (refreshLayout != null) {
        RefreshViewFactory.createRefreshView(refreshLayout)
    } else {
        null
    }

    val hostConfig = ListLayoutHostConfig().apply(config)

    refreshLayoutImpl?.setRefreshHandler(object : RefreshView.RefreshHandler() {
        override fun onRefresh() {
            hostConfig.onRefresh?.invoke()
        }

        override fun canRefresh(): Boolean {
            return true
        }
    })

    stateLayoutImpl.stateLayoutConfig.setStateRetryListener(object : OnRetryActionListener {
        override fun onRetry(state: Int) {
            hostConfig.onRetry?.invoke(state)
        }
    })

    loadMoreController?.setOnLoadMoreListener(object : OnLoadMoreListener {
        override fun onLoadMore() {
            hostConfig.onLoadMore?.invoke()
        }

        override fun canLoadMore() = true
    })

    return object : ListLayoutHost<T> {

        override val pager = AutoPaging(this, object : PagerSize {
            override fun getSize(): Int {
                return listDataHost.getListSize()
            }
        })

        override fun replaceData(data: List<T>) {
            listDataHost.replaceData(data)
        }

        override fun addData(data: List<T>) {
            listDataHost.addData(data)
        }

        override fun loadMoreCompleted(hasMore: Boolean) {
            loadMoreController?.loadCompleted(hasMore)
        }

        override fun loadMoreFailed() {
            loadMoreController?.loadFail()
        }

        override fun isEmpty(): Boolean {
            return listDataHost.isEmpty()
        }

        override fun getListSize(): Int {
            return listDataHost.getListSize()
        }

        override fun isLoadingMore(): Boolean {
            return loadMoreController?.isLoadingMore ?: false
        }

        override fun setLoadingMore() {
            loadMoreController?.setLoadingMore()
        }

        override var isLoadMoreEnable: Boolean
            get() = loadMoreController != null
            set(_) {
                Timber.w("setLoadMoreEnable() is not supported")
            }

        override fun autoRefresh() {
            refreshLayoutImpl?.autoRefresh()
        }

        override fun refreshCompleted() {
            refreshLayoutImpl?.refreshCompleted()
        }

        override fun isRefreshing(): Boolean {
            return refreshLayoutImpl?.isRefreshing() ?: false
        }

        override fun setRefreshing() {
            refreshLayoutImpl?.setRefreshing()
        }

        override var isRefreshEnable: Boolean
            get() = refreshLayoutImpl?.isRefreshEnable ?: false
            set(value) {
                refreshLayoutImpl?.isRefreshEnable = value
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

    }//object end.

}