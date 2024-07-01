package com.android.base.fragment.ui

import com.android.base.adapter.DataManager

interface SegmentedListDataHost<T> {

    fun replaceData(data: List<T>)

    fun addData(data: List<T>)

    fun isEmpty(): Boolean

    fun getListSize(): Int

}

interface SegmentedListLayoutHost<T, PageKey : Any> : StateLayoutHost, SegmentedListDataHost<T> {

    fun loadMoreCompleted(hasMore: Boolean)

    fun loadMoreFailed()

    fun isLoadingMore(): Boolean

    fun setLoadingMore()

    var isLoadMoreEnable: Boolean

    val paging: Paging<PageKey>

}

fun <T> DataManager<T>.toSegmentedListDataHost(): SegmentedListDataHost<T> {
    return object : SegmentedListDataHost<T> {
        override fun replaceData(data: List<T>) {
            replaceAll(data)
        }

        override fun addData(data: List<T>) {
            addItems(data)
        }

        override fun isEmpty(): Boolean {
            return this@toSegmentedListDataHost.isEmpty()
        }

        override fun getListSize(): Int {
            return getDataSize()
        }
    }
}