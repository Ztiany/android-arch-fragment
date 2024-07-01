package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.state.BaseStateFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.RefreshLoadMoreView
import com.android.base.fragment.ui.SegmentedListDataHost
import com.android.base.fragment.ui.SegmentedListLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import kotlin.properties.Delegates

/**
 * A versatile base fragment for managing list interfaces using [RefreshLoadMoreView], supporting both pull-to-refresh and infinite scrolling.
 * Unlike [BaseListFragment], which wraps a [RecyclerView.Adapter] to implement its functionality, [BaseList2Fragment] provides flexibility by not being restricted
 * to any specific view type.
 *
 * Usage remains suitable for segmented data submission, similar to [BaseListFragment].
 *
 * Note: the layout requirements are the same as requirements of [BaseStateFragment].
 *
 * @param T The type of data used in the current list.
 * @param VB The ViewBinding type associated with the fragment.
 * @author Ztiany
 * @see BaseListFragment
 * @see BaseStateFragment
 */
abstract class BaseList2Fragment<T, PageKey : Any, VB : ViewBinding> : BaseUIFragment<VB>() {

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
                this@BaseList2Fragment.onLoadMore()
            }
            this.onRefresh = {
                this@BaseList2Fragment.onRefresh()
            }
            this.onRetry = {
                this@BaseList2Fragment.onRetry(it)
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