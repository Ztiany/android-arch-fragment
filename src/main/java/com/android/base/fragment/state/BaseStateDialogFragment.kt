package com.android.base.fragment.state

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutHost

/**
 * @author Ztiany
 * @see BaseStateFragment
 */
abstract class BaseStateDialogFragment<VB : ViewBinding> : BaseUIDialogFragment<VB>(), StateLayoutHost {

    private lateinit var stateLayoutHostImpl: StateLayoutHost

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        stateLayoutHostImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BaseStateDialogFragment.onRefresh()
            }
            this.onRetry = {
                this@BaseStateDialogFragment.onRetry(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshCompleted()
    }

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
        if (stateLayoutHostImpl.isRefreshEnable) {
            if (!isRefreshing()) {
                stateLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    override var isRefreshEnable = stateLayoutHostImpl.isRefreshEnable

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