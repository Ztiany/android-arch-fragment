package com.android.base.fragment.state

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.R
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.RefreshView
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutConfig.CONTENT
import com.android.base.fragment.ui.StateLayoutConfig.EMPTY
import com.android.base.fragment.ui.StateLayoutConfig.ERROR
import com.android.base.fragment.ui.StateLayoutConfig.LOADING
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/**
 * BaseStateFragment is a Fragment that manages various states such as [CONTENT], [LOADING], [ERROR], and [EMPTY],
 * along with support for pull-to-refresh functionality, designed to be displayed in a StateLayout-enabled ViewGroup
 * with the ID [R.id.base_state_layout]. The ViewGroup must implement [com.android.base.fragment.ui.StateLayout].
 * Additionally, if your layout includes a [RefreshView] for pull-to-refresh, its ID must be set to [R.id.base_refresh_layout].
 * Omitting the RefreshView indicates no pull-to-refresh functionality is needed. By default, both retry attempts and
 * pull-to-refresh actions invoke the [onRefresh] method, which can be overridden by subclasses. For detailed usage,
 * refer to the README.md of this module.
 *
 * Use [handleState] or [handleFlowDataState] to manage your loaded data states.
 *
 * @author Ztiany
 */
abstract class BaseStateFragment<VB : ViewBinding> : BaseUIFragment<VB>() {

    private lateinit var stateLayoutHostImpl: StateLayoutHost

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        stateLayoutHostImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BaseStateFragment.onRefresh()
            }
            this.onRetry = {
                this@BaseStateFragment.onRetry(it)
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

    val stateLayoutController: StateLayoutHost
        get() = stateLayoutHostImpl

}