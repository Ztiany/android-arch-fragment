package com.android.base.fragment.state

import android.view.View
import com.android.base.fragment.ui.RefreshView
import com.android.base.fragment.ui.RefreshViewFactory
import com.android.base.fragment.ui.OnRetryActionListener
import com.android.base.fragment.ui.StateLayout
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutHost

class StateLayoutHostConfig {
    var onRetry: ((state: Int) -> Unit)? = null
    var onRefresh: (() -> Unit)? = null
}

/** It is useful when there is more than one state layout in a fragment. */
fun buildStateLayoutHost(
    stateLayout: View,
    refreshView: View? = null,
    config: StateLayoutHostConfig.() -> Unit,
): StateLayoutHost {

    val stateLayoutImpl =
        (stateLayout as? StateLayout) ?: throw IllegalStateException("Please declare a view that implements StateLayout in your layout.")

    val refreshViewImpl = if (refreshView != null) {
        RefreshViewFactory.createRefreshView(refreshView)
    } else {
        null
    }

    val hostConfig = StateLayoutHostConfig().apply(config)

    refreshViewImpl?.setRefreshHandler(object : RefreshView.RefreshHandler() {
        override fun onRefresh() {
            hostConfig.onRefresh?.invoke()
        }
    })

    stateLayoutImpl.stateLayoutConfig.setStateRetryListener(object : OnRetryActionListener {
        override fun onRetry(state: Int) {
            hostConfig.onRetry?.invoke(state)
        }
    })

    return object : StateLayoutHost {

        override fun autoRefresh() {
            refreshViewImpl?.autoRefresh()
        }

        override fun refreshCompleted() {
            refreshViewImpl?.refreshCompleted()
        }

        override fun setRefreshing() {
            refreshViewImpl?.setRefreshing()
        }

        override fun isRefreshing(): Boolean {
            return refreshViewImpl?.isRefreshing() ?: false
        }

        override var isRefreshEnable: Boolean
            get() = refreshViewImpl?.isRefreshEnable ?: false
            set(value) {
                refreshViewImpl?.isRefreshEnable = value
            }

        override fun showContentLayout() {
            refreshCompleted()
            stateLayoutImpl.showContentLayout()
        }

        override fun showLoadingLayout() {
            stateLayoutImpl.showLoadingLayout()
        }

        override fun showEmptyLayout() {
            refreshCompleted()
            stateLayoutImpl.showEmptyLayout()
        }

        override fun showErrorLayout() {
            refreshCompleted()
            stateLayoutImpl.showErrorLayout()
        }

        override fun showRequesting() {
            stateLayoutImpl.showRequesting()
        }

        override fun showBlank() {
            stateLayoutImpl.showBlank()
        }

        override fun showNetErrorLayout() {
            refreshCompleted()
            stateLayoutImpl.showNetErrorLayout()
        }

        override fun showServerErrorLayout() {
            refreshCompleted()
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