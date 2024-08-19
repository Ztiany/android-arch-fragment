package com.android.base.fragment.list.paging3

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.state.buildStateLayoutHost
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.PagingLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import com.android.base.fragment.ui.toPagingLayoutHost

/**
 * A [Fragment] works with paging3. use [handlePagingData] to manage you loaded data.
 */
abstract class BasePagingFragment<VB : ViewBinding> : BaseUIFragment<VB>() {

    private lateinit var pagingLayoutImpl: PagingLayoutHost

    override fun internalOnSetupCreatedView(view: View, savedInstanceState: Bundle?) {
        pagingLayoutImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BasePagingFragment.onRefresh()
            }
            this.onRetry = {
                this@BasePagingFragment.onRetry(it)
            }
        }.toPagingLayoutHost()
    }

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
        if (!internalRetryByAutoRefresh) {
            onRefresh()
            return
        }

        if (pagingLayoutImpl.isRefreshEnable) {
            if (!pagingLayoutImpl.isRefreshing()) {
                pagingLayoutImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    val pagingController: PagingLayoutHost
        get() = pagingLayoutImpl

}