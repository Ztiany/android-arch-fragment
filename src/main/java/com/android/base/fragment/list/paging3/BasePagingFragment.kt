package com.android.base.fragment.list.paging3

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.state.buildStateLayoutHost
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.PagingHost
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

// TODO: implement it.
// Reference 1: <https://github.com/android/architecture-components-samples/tree/main/PagingWithNetworkSample>
// Reference 1: <https://github.com/airbnb/epoxy/wiki/Paging-Support>
abstract class BasePagingFragment<T, VB : ViewBinding> : BaseUIFragment<VB>(), PagingHost {

    private lateinit var stateLayoutHostImpl: StateLayoutHost

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        stateLayoutHostImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BasePagingFragment.onRefresh()
            }
            this.onRetry = {
                this@BasePagingFragment.onRetry(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshCompleted()
    }

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
        if (!internalRetryByAutoRefresh) {
            onRefresh()
            return
        }

        if (stateLayoutHostImpl.isRefreshEnable) {
            if (!isRefreshing()) {
                stateLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    override var isRefreshEnable: Boolean
        get() = stateLayoutHostImpl.isRefreshEnable
        set(value) {
            stateLayoutHostImpl.isRefreshEnable = value
        }

    override fun getStateLayoutConfig(): StateLayoutConfig = stateLayoutHostImpl.stateLayoutConfig

    override fun setRefreshing() {
        stateLayoutHostImpl.setRefreshing()
    }

    override fun isRefreshing(): Boolean {
        return stateLayoutHostImpl.isRefreshing()
    }

    override fun refreshCompleted() = stateLayoutHostImpl.refreshCompleted()

    override fun autoRefresh() = stateLayoutHostImpl.autoRefresh()

    override fun showContentLayout() = stateLayoutHostImpl.showContentLayout()

    override fun showLoadingLayout() = stateLayoutHostImpl.showLoadingLayout()

    override fun showEmptyLayout() = stateLayoutHostImpl.showEmptyLayout()

    override fun showErrorLayout() = stateLayoutHostImpl.showErrorLayout()

    override fun showRequesting() = stateLayoutHostImpl.showRequesting()

    override fun showBlank() = stateLayoutHostImpl.showBlank()

    override fun showNetErrorLayout() = stateLayoutHostImpl.showNetErrorLayout()

    override fun showServerErrorLayout() = stateLayoutHostImpl.showServerErrorLayout()

    override fun currentStatus() = stateLayoutHostImpl.currentStatus()

    @Suppress("UNUSED")
    companion object {
        const val CONTENT = StateLayoutConfig.CONTENT
        const val LOADING = StateLayoutConfig.LOADING
        const val ERROR = StateLayoutConfig.ERROR
        const val EMPTY = StateLayoutConfig.EMPTY
        const val NET_ERROR = StateLayoutConfig.NET_ERROR
        const val SERVER_ERROR = StateLayoutConfig.SERVER_ERROR
    }

}