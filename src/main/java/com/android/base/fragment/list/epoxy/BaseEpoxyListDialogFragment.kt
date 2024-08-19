package com.android.base.fragment.list.epoxy

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.ListDataHost
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import com.ztiany.loadmore.adapter.LoadMoreController
import kotlin.properties.Delegates

/**
 * @see BaseEpoxyListFragment
 */
abstract class BaseEpoxyListDialogFragment<T, VB : ViewBinding> : BaseUIDialogFragment<VB>(), ListLayoutHost<T> {

    private var loadMoreImpl: LoadMoreController? = null

    private var listLayoutHostImpl: ListLayoutHost<T> by Delegates.notNull()

    override fun internalOnSetupCreatedView(view: View, savedInstanceState: Bundle?) {
        listLayoutHostImpl = provideListImplementation(view, savedInstanceState)
    }

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
     *  2. You should call [setUpList] to return a real [ListLayoutHost].
     */
    abstract fun provideListImplementation(view: View, savedInstanceState: Bundle?): ListLayoutHost<T>

    /**
     * For the parameter [listDataHost], you could have a class inherited from [ListEpoxyController].
     */
    protected fun setUpList(
        listDataHost: ListDataHost<T>,
        loadMoreController: LoadMoreController? = null,
    ): ListLayoutHost<T> {
        this.loadMoreImpl = loadMoreController

        return buildListLayoutHost(
            listDataHost,
            loadMoreImpl,
            vb.root.findViewById(CommonId.STATE_ID),
            vb.root.findViewById(CommonId.REFRESH_ID)
        ) {

            this.onRetry = {
                this@BaseEpoxyListDialogFragment.onRetry(it)
            }
            this.onRefresh = {
                this@BaseEpoxyListDialogFragment.onRefresh()
            }
            this.onLoadMore = {
                this@BaseEpoxyListDialogFragment.onLoadMore()
            }
        }
    }

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
        if (!internalRetryByAutoRefresh) {
            onRefresh()
            return
        }

        if (listLayoutHostImpl.isRefreshEnable) {
            if (!listLayoutHostImpl.isRefreshing()) {
                listLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    protected open fun onLoadMore() {}

    val loadMoreController: LoadMoreController
        get() = loadMoreImpl ?: throw NullPointerException("You didn't enable load-more.")

    val listController: ListLayoutHost<T>
        get() = listLayoutHostImpl

}