package com.android.base.fragment.ui

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

internal class SwipeRefreshView(
    private val swipeRefreshLayout: SwipeRefreshLayout,
) : RefreshView {

    private var refreshHandler: RefreshView.RefreshHandler? = null

    override fun autoRefresh() {
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
            doRefresh()
        }
    }

    override fun setRefreshing() {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun refreshCompleted() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun setRefreshHandler(refreshHandler: RefreshView.RefreshHandler?) {
        this.refreshHandler = refreshHandler
        swipeRefreshLayout.setOnRefreshListener { doRefresh() }
    }

    override fun isRefreshing(): Boolean {
        return swipeRefreshLayout.isRefreshing
    }

    private fun doRefresh() {
        if (refreshHandler?.canRefresh() == true) {
            refreshHandler?.onRefresh()
        } else {
            swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = false }
        }
    }

    override var isRefreshEnable: Boolean
        get() = swipeRefreshLayout.isEnabled
        set(enable) {
            swipeRefreshLayout.isEnabled = enable
        }

}