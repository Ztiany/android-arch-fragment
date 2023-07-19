package com.android.base.fragment.epoxy

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.list.buildListLayoutHost
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.ListDataHost
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.StateLayoutConfig
import com.ztiany.loadmore.adapter.LoadMoreController
import kotlin.properties.Delegates

/** This ListFragment works with [epoxy](https://github.com/airbnb/epoxy). You can use [handleListStateWithViewLifecycle] to handle received list data. */
abstract class BaseEpoxyListFragment<T, VB : ViewBinding> : BaseUIFragment<VB>(), ListLayoutHost<T> {

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
                this@BaseEpoxyListFragment.onRetry(it)
            }
            this.onRefresh = {
                this@BaseEpoxyListFragment.onRefresh()
            }
            this.onLoadMore = {
                this@BaseEpoxyListFragment.onLoadMore()
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