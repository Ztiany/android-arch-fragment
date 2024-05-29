package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.viewbinding.ViewBinding
import com.android.base.adapter.DataManager
import com.android.base.adapter.recycler.BaseRecyclerAdapter
import com.android.base.core.AndroidSword
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.list.handleListData
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.ListDataHost
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.StateLayoutConfig
import com.ztiany.loadmore.adapter.LoadMoreAdapter
import com.ztiany.loadmore.adapter.LoadMoreController
import kotlin.properties.Delegates

/**
 * 通用的基于 RecyclerView 的列表界面，支持下拉刷新和加载更多。其一般的使用方式如下：
 *
 * 1. 列表数据由 [DataManager] 来管理，[DataManager] 一般是由 Adapter 实现，比如 [BaseRecyclerAdapter]；
 * 2. 在 [BaseListFragment] 中通过 [lifecycleScope] 来分段加载数据，然后累加到 [DataManager]  中；
 * 3. 在 [BaseListFragment] 被销毁重建（因配置发送改变）后，所有的 UI 状态和已经加载的数据都会被重置，未完成的加载也会被取消。
 * 4. 所有对列表的 Item 的操作，都在 [DataManager] 中进行。
 *
 * 以上是典型的命令式编程，适合**分段式**提交列表数据，可以使用 [handleListData] 来处理列表数据。比如：
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
 * 但是由于分段加载分段提交的方式的局限性，以上方式并没有利用到 [ViewModel] 的特性，如果想要采用类似 Paging3 架构的那种的
 * 方式，列表数据放在 [ViewModel] 中管理，每次都全量提交数据，由框架本身来通知列表刷新（即完全使用数据驱动 UI），则可以
 * 使用 [fragment-epoxy] 模块。
 *
 * @param <T> 当前列表使用的数据类型。
 * @author Ztiany
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