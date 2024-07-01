package com.android.base.fragment.ui

interface ListDataHost<T> {

    fun submitData(data: List<T>)

    fun isEmpty(): Boolean

    fun getListSize(): Int

}

/**
 * 列表视图行为。
 */
interface ListLayoutHost<T> : StateLayoutHost, ListDataHost<T> {

    fun loadMoreCompleted(hasMore: Boolean)

    fun loadMoreFailed()

    fun isLoadingMore(): Boolean

    fun setLoadingMore()

    var isLoadMoreEnable: Boolean

}
