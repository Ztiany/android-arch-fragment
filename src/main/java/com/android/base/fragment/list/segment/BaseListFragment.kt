package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import com.android.base.adapter.DataManager
import com.android.base.adapter.recycler.segment.BaseRecyclerAdapter
import com.android.base.core.AndroidSword
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.list.epoxy.BaseEpoxyListFragment
import com.android.base.fragment.list.handleListData
import com.android.base.fragment.list.paging3.BasePagingFragment
import com.android.base.fragment.state.BaseStateFragment
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
 * A generic base fragment for managing RecyclerView-based list interfaces with support for pull-to-refresh and infinite scrolling.
 * Usage of this fragment involves:
 *
 * 1. Managing list data through a [DataManager], typically implemented by the adapter such as [BaseRecyclerAdapter].
 * 2. Utilizing [lifecycleScope] within [BaseListFragment] to load data in segments, incrementally updating the [DataManager].
 * 3. Upon destruction and recreation of [BaseListFragment] due to configuration changes, all UI states and loaded data are reset,
 *    and ongoing loading operations are cancelled.
 * 4. All operations related to list items are performed within the [DataManager].
 *
 * This approach is suitable for imperative programming with segmented data submission. you can use [handleListData] for data list processing, for example:
 *
 * ```kotlin
 *     class RecordsViewModel constructor(
 *              private val xxxRepository: PayRepository
 *      ) : ViewModel() {
 *
 *              suspend fun loadRecords(page: Int, pageSize: Int): List<ListItem>? {
 *                  return xxxRepository.loadListData(page, pageSize)
 *              }
 *
 *       }
 *
 *    class XXXListFragment : CommonBaseListFragment<PayRecord, XXXLayoutBinding>() {
 *
 *          override fun onRefresh() {
 *                 listLayoutController.handleListStartRefresh()
 *                loadList(paging.start)
 *           }
 *
 *          override fun onLoadMore() {
 *                listLayoutController.handleListStartLoadMore()
 *                loadList(paging.next)
 *           }
 *
 *          fun loadList(loadingPage: Int) {
 *                  lifecycleScope.launch {
 *                      try {
 *                         handleListLoading()
 *                         val payRecords = viewModel.loadListData(loadingPage, paging.size)
 *                         listLayoutController.handleListResult(payRecords)
 *                      } catch (e: XXXException) {
 *                         listLayoutController.handleListError(e)
 *                      }
 *                  }
 *           }
 *
 *    }
 * ```
 *
 * However, due to the limitations of segmented loading and submission, this method does not fully utilize the features of [ViewModel].
 * For a more sophisticated approach resembling the Paging3 architecture, where list data is managed within the [ViewModel] and fully submitted
 * each time to drive UI updates based on data, consider using the [BasePagingFragment] or [BaseEpoxyListFragment].
 *
 * Note: the layout requirements are the same as requirements of [BaseStateFragment].
 *
 * @param T The type of data used in the current list.
 * @param VB The ViewBinding type associated with the fragment.
 * @author Ztiany
 * @see BaseStateFragment
 */
abstract class BaseListFragment<T, PageKey : Any, VB : ViewBinding> : BaseUIFragment<VB>() {

    private var loadMoreImpl: LoadMoreController? = null

    private var listLayoutHostImpl: SegmentedListLayoutHost<T, PageKey> by Delegates.notNull()

    override fun internalOnSetupCreatedView(view: View, savedInstanceState: Bundle?) {
        listLayoutHostImpl = provideListImplementation(view, savedInstanceState)
    }

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
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
                this@BaseListFragment.onRetry(it)
            }
            onRefresh = {
                this@BaseListFragment.onRefresh()
            }
            onLoadMore = {
                this@BaseListFragment.onLoadMore()
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

    protected val listController: SegmentedListLayoutHost<T, PageKey>
        get() = listLayoutHostImpl

    protected val paging: Paging<PageKey>
        get() = listLayoutHostImpl.paging

}