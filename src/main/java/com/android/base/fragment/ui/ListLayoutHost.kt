package com.android.base.fragment.ui

import com.android.base.adapter.DataManager

interface ListDataHost<T> {

    fun replaceData(data: List<T>)

    fun addData(data: List<T>)

    fun isEmpty(): Boolean

    fun getListSize(): Int

}

/**
 * 列表视图行为。
 */
interface ListLayoutHost<T> : StateLayoutHost, ListDataHost<T> {

    val paging: Paging

    fun loadMoreCompleted(hasMore: Boolean)

    fun loadMoreFailed()

    fun isLoadingMore(): Boolean

    fun setLoadingMore()

    var isLoadMoreEnable: Boolean

}

fun <T> DataManager<T>.toListDataHost(): ListDataHost<T> {
    return object : ListDataHost<T> {
        override fun replaceData(data: List<T>) {
            replaceAll(data)
        }

        override fun addData(data: List<T>) {
            addItems(data)
        }

        override fun isEmpty(): Boolean {
            return this@toListDataHost.isEmpty()
        }

        override fun getListSize(): Int {
            return getDataSize()
        }
    }
}