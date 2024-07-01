package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.SegmentedListDataHost
import com.android.base.fragment.ui.SegmentedListLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import kotlin.properties.Delegates

/**
 * @author Ztiany
 * @see [BaseList2Fragment]
 */
abstract class BaseList2DialogFragment<T, PageKey : Any, VB : ViewBinding> : BaseUIDialogFragment<VB>() {

    private var listLayoutHostImpl: SegmentedListLayoutHost<T, PageKey> by Delegates.notNull()

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        listLayoutHostImpl = provideListImplementation(view, savedInstanceState)
    }

    /**
     *  1. This method will be called before [onViewCreated] and [onSetUpCreatedView].
     *  2. You should call [setUpList] to return a real [ListLayoutHost].
     */
    abstract fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, PageKey>

    protected fun setUpList(
        listDataHost: SegmentedListDataHost<T>,
        paging: Paging<PageKey>,
    ): SegmentedListLayoutHost<T, PageKey> {
        return buildSegmentedListLayoutHost2(
            listDataHost,
            paging,
            vb.root.findViewById(CommonId.STATE_ID),
            vb.root.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onLoadMore = {
                this@BaseList2DialogFragment.onLoadMore()
            }
            this.onRefresh = {
                this@BaseList2DialogFragment.onRefresh()
            }
            this.onRetry = {
                this@BaseList2DialogFragment.onRetry(it)
            }
        }
    }

    protected open fun onRetry(@StateLayoutConfig.RetryableState state: Int) {
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

    val paging: Paging<PageKey>
        get() = listLayoutHostImpl.paging

    protected val listLayoutController: SegmentedListLayoutHost<T, PageKey>
        get() = listLayoutHostImpl

}