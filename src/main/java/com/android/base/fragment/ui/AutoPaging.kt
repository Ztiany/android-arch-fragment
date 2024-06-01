package com.android.base.fragment.ui

import timber.log.Timber

/** Used to provide a pager's total item count. */
typealias TotalSize = () -> Int

/**
 * @author Ztiany
 */
class AutoPaging(
    override val size: Int = defaultPagingSize,
    override val start: Int = defaultPagingStart,
    /** callback for providing the list's total size. */
    private val totalSize: TotalSize,
) : Paging() {

    private var accumulatedTotal = totalSize()

    private var accumulatedPage = start

    override val current: Int
        get() = accumulatedPage

    override val total: Int
        get() = accumulatedTotal

    override val next: Int
        get() = current + 1

    override fun onPageAppended(appendedSize: Int) {
        accumulatedTotal += appendedSize
        accumulatedPage++
        Timber.d("onPageAppended: accumulatedTotal = $accumulatedTotal, accumulatedPage = $accumulatedPage")
        // it is only for test
        calculatePageNumber(totalSize(), size, start)
    }

    override fun onPageRefreshed(loadedSize: Int) {
        accumulatedTotal = loadedSize
        accumulatedPage = start
        Timber.d("onPageRefreshed: accumulatedTotal = $accumulatedTotal, accumulatedPage = $accumulatedPage")
        // it is only for test
        calculatePageNumber(totalSize(), size, start)
    }

    /**
     * 根据 page size 计算当前的页码。
     *
     * - [total] 列表当前的总数据量
     * - [pagingSize] 分页大小
     * - [pagingStart] 分页起始页码，0 或 1
     */
    private fun calculatePageNumber(total: Int, pagingSize: Int, pagingStart: Int): Int {
        /*                          s=1        s=0
          19/20     = 0          1          0
          21/20     = 1          2          1
          54/20     = 2          3          2
          64/20     = 3          4          3
         */
        var pageNumber: Int
        val pageSize: Int = pagingSize
        val pageStart: Int = pagingStart
        when (pageStart) {
            0 -> {
                pageNumber = (total / pageSize) - 1
                pageNumber = if (pageNumber < 0) 0 else pageNumber
            }

            1 -> {
                pageNumber = (total / pageSize)
                pageNumber = if (pageNumber < 1) 1 else pageNumber
            }

            else -> {
                throw RuntimeException("pageStart must be 0 or 1")
            }
        }
        Timber.d("calculatePageNumber: pageStart=1, dataSize=%d, pageSize=%d, pageNumber=%d", total, pagingSize, pageNumber)
        return pageNumber
    }

}
