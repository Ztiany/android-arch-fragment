package com.android.base.fragment.ui

import timber.log.Timber

/**
 * @author Ztiany
 */
abstract class Paging @JvmOverloads constructor(
    var pageStart: Int = defaultPageStart,
    var pageSize: Int = defaultPageSize,
) {

    var pageToken: Any? = null

    fun hasMore(size: Int): Boolean {
        return size >= pageSize
    }

    /**
     * 根据 page size 计算当前的页码。
     */
    fun calcPageNumber(dataSize: Int): Int {
        var pageNumber: Int
        val pageSize = pageSize
        when (pageStart) {
            0 -> {
                pageNumber = dataSize / pageSize - 1
                Timber.d("pageStart=0, dataSize=%d, pageSize=%d, nextPageNumber=%d", dataSize, pageSize, pageNumber)
            }

            1 -> {
                pageNumber = dataSize / pageSize
                pageNumber += 1
                Timber.d("pageStart=1, dataSize=%d, pageSize=%d, nextPageNumber=%d", dataSize, pageSize, pageNumber)
            }

            else -> {
                throw RuntimeException("pageStart must be 0 or 1")
            }
        }
        return pageNumber
    }

    fun setPaging(pageStart: Int, pageSize: Int) {
        this.pageStart = pageStart
        this.pageSize = pageSize
    }

    abstract val currentPage: Int

    abstract val loadingPage: Int

    abstract val itemCount: Int

    companion object {
        var defaultPageStart = 0
        var defaultPageSize = 20
    }

}