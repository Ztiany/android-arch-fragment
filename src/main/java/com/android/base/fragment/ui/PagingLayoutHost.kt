package com.android.base.fragment.ui

import com.android.base.fragment.list.paging3.BasePagingFragment

/** Serve for [BasePagingFragment]. */
interface PagingLayoutHost : StateLayoutHost

internal fun StateLayoutHost.toPagingLayoutHost(): PagingLayoutHost {
    val stateLayoutHost = this
    return object : PagingLayoutHost {
        override fun autoRefresh() {
            stateLayoutHost.autoRefresh()
        }

        override fun setRefreshing() {
            stateLayoutHost.setRefreshing()
        }

        override fun refreshCompleted() {
            stateLayoutHost.refreshCompleted()
        }

        override fun isRefreshing(): Boolean {
            return stateLayoutHost.isRefreshing()
        }

        override var isRefreshEnable: Boolean
            get() = stateLayoutHost.isRefreshEnable
            set(value) {
                stateLayoutHost.isRefreshEnable = value
            }

        override fun showContentLayout() {
            stateLayoutHost.showContentLayout()
        }

        override fun showLoadingLayout() {
            stateLayoutHost.showLoadingLayout()
        }

        override fun showEmptyLayout() {
            stateLayoutHost.showEmptyLayout()
        }

        override fun showErrorLayout() {
            stateLayoutHost.showErrorLayout()
        }

        override fun showRequesting() {
            stateLayoutHost.showRequesting()
        }

        override fun showBlank() {
            stateLayoutHost.showBlank()
        }

        override fun showNetErrorLayout() {
            stateLayoutHost.showNetErrorLayout()
        }

        override fun showServerErrorLayout() {
            stateLayoutHost.showServerErrorLayout()
        }

        override fun getStateLayoutConfig(): StateLayoutConfig {
            return stateLayoutHost.getStateLayoutConfig()
        }

        override fun currentStatus(): Int {
            return stateLayoutHost.currentStatus()
        }

    }
}