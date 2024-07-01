package com.android.base.fragment.list.paging3

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.state.buildStateLayoutHost
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.PagingLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import com.android.base.fragment.ui.toPagingLayoutHost


/**
 *  @see BasePagingFragment
 */
abstract class BasePagingDialogFragment<VB : ViewBinding> : BaseUIDialogFragment<VB>(), PagingLayoutHost {

    private lateinit var pagingLayoutImpl: PagingLayoutHost

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        pagingLayoutImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BasePagingDialogFragment.onRefresh()
            }
            this.onRetry = {
                this@BasePagingDialogFragment.onRetry(it)
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

    val pagingLayoutController: PagingLayoutHost
        get() = pagingLayoutImpl

}