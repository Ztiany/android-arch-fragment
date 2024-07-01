package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import com.android.base.adapter.DataManager
import com.android.base.adapter.recycler.segment.BaseRecyclerAdapter
import com.android.base.core.AndroidSword
import com.android.base.fragment.base.BaseUIDialogFragment
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.SegmentedListDataHost
import com.android.base.fragment.ui.SegmentedListLayoutHost
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.toSegmentedListDataHost
import com.ztiany.loadmore.adapter.LoadMoreAdapter
import com.ztiany.loadmore.adapter.LoadMoreController
import kotlin.properties.Delegates

/**
 * @author Ztiany
 * @see [BaseListFragment]
 */
abstract class BaseListDialogFragment<T, PageKey : Any, VB : ViewBinding> : BaseUIDialogFragment<VB>() {

    private var loadMoreImpl: LoadMoreController? = null

    private var listLayoutHostImpl: SegmentedListLayoutHost<T, PageKey> by Delegates.notNull()

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        listLayoutHostImpl = provideListImplementation(view, savedInstanceState)
    }

    /**
     *  1. This method will be called before [onViewCreated] and [onSetUpCreatedView].
     *  2. You should call [setUpList] to return a real [SegmentedListLayoutHost].
     */
    abstract fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, PageKey>

    /**
     * Call this method before calling to [setUpList]. And assign [RecyclerView]'s [Adapter] with the return value.
     */
    protected fun enableLoadMore(
        adapter: Adapter<*>,
        triggerLoadMoreByScroll: Boolean = AndroidSword.loadMoreTriggerByScroll,
    ): Adapter<*> {
        return LoadMoreAdapter.wrap(adapter, triggerLoadMoreByScroll).apply {
            loadMoreImpl = this
        }
    }

    /**
     * For parameter [listDataHost] Usually, It's implemented by your [RecyclerView.Adapter]. If your Adapter has
     * implemented [DataManager] life [BaseRecyclerAdapter], you can use [toSegmentedListDataHost] to convert your
     * Adapter to a [SegmentedListDataHost].
     */
    protected fun setUpList(listDataHost: SegmentedListDataHost<T>, paging: Paging<PageKey>): SegmentedListLayoutHost<T, PageKey> {
        return buildSegmentedListLayoutHost(
            listDataHost,
            loadMoreImpl,
            paging,
            vb.root.findViewById(CommonId.STATE_ID),
            vb.root.findViewById(CommonId.REFRESH_ID)
        ) {
            onRetry = {
                this@BaseListDialogFragment.onRetry(it)
            }
            onRefresh = {
                this@BaseListDialogFragment.onRefresh()
            }
            onLoadMore = {
                this@BaseListDialogFragment.onLoadMore()
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

    protected val loadMoreController: LoadMoreController
        get() = loadMoreImpl ?: throw NullPointerException("You didn't enable load-more.")

    protected val listLayoutController: SegmentedListLayoutHost<T, PageKey>
        get() = listLayoutHostImpl

    protected val paging: Paging<PageKey>
        get() = listLayoutHostImpl.paging

}