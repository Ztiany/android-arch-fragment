package com.android.base.fragment.state

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/**
 * @author Ztiany
 * @see BaseStateFragment
 */
abstract class BaseStateDialogFragment<VB : ViewBinding> : BaseUIDialogFragment<VB>() {

    private lateinit var stateLayoutHostImpl: StateLayoutHost

    override fun internalOnSetupCreatedView(view: View, savedInstanceState: Bundle?) {
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

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
        if (!internalRetryByAutoRefresh) {
            onRefresh()
            return
        }

        if (stateLayoutHostImpl.isRefreshEnable) {
            if (!stateLayoutHostImpl.isRefreshing()) {
                stateLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    val stateController: StateLayoutHost
        get() = stateLayoutHostImpl

}