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
import com.android.base.fragment.ui.ListDataHost
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.StateLayoutConfig
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
 *    class XXXListFragment : BaseListFragment<PayRecord, XXXLayoutBinding>() {
 *
 *          override fun onRefresh() {
 *                loadList(paging.start)
 *           }
 *
 *          override fun onLoadMore() {
 *                loadList(paging.next)
 *           }
 *
 *          fun loadList(loadingPage: Int) {
 *                  lifecycleScope.launch {
 *                      try {
 *                         handleListLoading()
 *                         val payRecords = viewModel.loadListData(loadingPage, paging.size)
 *                         handleListResult(payRecords)
 *                      } catch (e: XXXException) {
 *                         handleListError(e)
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
abstract class BaseListFragment<T, VB : ViewBinding> : BaseUIFragment<VB>(), ListLayoutHost<T> {

    private var loadMoreImpl: LoadMoreController? = null

    private var listLayoutHostImpl: ListLayoutHost<T> by Delegates.notNull()

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        listLayoutHostImpl = provideListImplementation(view, savedInstanceState)
    }

    /**
     *  1. This method will be called before [onViewCreated] and [onSetUpCreatedView].
     *  2. You should invoke [setUpList] to return a real [ListLayoutHost].
     */
    abstract fun provideListImplementation(view: View, savedInstanceState: Bundle?): ListLayoutHost<T>

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

    protected fun setUpList(listDataHost: ListDataHost<T>): ListLayoutHost<T> {
        return buildListLayoutHost(
            listDataHost,
            loadMoreImpl,
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
            if (!isRefreshing()) {
                listLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    protected open fun onLoadMore() {}

    override fun replaceData(data: List<T>) = listLayoutHostImpl.replaceData(data)

    override fun addData(data: List<T>) = listLayoutHostImpl.addData(data)

    override fun isEmpty(): Boolean {
        return listLayoutHostImpl.isEmpty()
    }

    override fun getListSize(): Int {
        return listLayoutHostImpl.getListSize()
    }

    override fun isLoadingMore(): Boolean {
        return listLayoutHostImpl.isLoadingMore()
    }

    override fun setLoadingMore() {
        listLayoutHostImpl.setLoadingMore()
    }

    override fun setRefreshing() {
        listLayoutHostImpl.setRefreshing()
    }

    override fun isRefreshing(): Boolean {
        return listLayoutHostImpl.isRefreshing()
    }

    override val paging: Paging
        get() = listLayoutHostImpl.paging

    val loadMoreController: LoadMoreController
        get() = loadMoreImpl ?: throw NullPointerException("You didn't enable load-more.")

    override fun loadMoreCompleted(hasMore: Boolean) {
        loadMoreImpl?.loadCompleted(hasMore)
    }

    override fun loadMoreFailed() {
        loadMoreImpl?.loadFail()
    }

    override var isLoadMoreEnable: Boolean
        get() = listLayoutHostImpl.isLoadMoreEnable
        set(value) {
            listLayoutHostImpl.isLoadMoreEnable = value
        }

    override fun autoRefresh() {
        listLayoutHostImpl.autoRefresh()
    }

    override fun refreshCompleted() {
        listLayoutHostImpl.refreshCompleted()
    }

    override fun showContentLayout() {
        listLayoutHostImpl.showContentLayout()
    }

    override fun showLoadingLayout() {
        listLayoutHostImpl.showLoadingLayout()
    }

    override fun showEmptyLayout() {
        listLayoutHostImpl.showEmptyLayout()
    }

    override fun showErrorLayout() {
        listLayoutHostImpl.showErrorLayout()
    }

    override fun showRequesting() {
        listLayoutHostImpl.showRequesting()
    }

    override fun showBlank() {
        listLayoutHostImpl.showBlank()
    }

    override fun showNetErrorLayout() {
        listLayoutHostImpl.showNetErrorLayout()
    }

    override fun showServerErrorLayout() {
        listLayoutHostImpl.showServerErrorLayout()
    }

    override fun getStateLayoutConfig(): StateLayoutConfig {
        return listLayoutHostImpl.stateLayoutConfig
    }

    @StateLayoutConfig.ViewState
    override fun currentStatus(): Int {
        return listLayoutHostImpl.currentStatus()
    }

    override var isRefreshEnable: Boolean
        get() = listLayoutHostImpl.isRefreshEnable
        set(value) {
            listLayoutHostImpl.isRefreshEnable = value
        }

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