package com.android.base.fragment.ui

import timber.log.Timber

typealias PageSize = () -> Int

/**
 * @author Ztiany
 */
class AutoPaging(
    /** callback for providing list total size. */
    private val pageSizeGetter: PageSize,
) : Paging() {

    override val current: Int
        get() = calculatePageNumber(pageSizeGetter(), size, start)

    override val next: Int
        get() = current + 1

    override val total: Int
        get() = pageSizeGetter()

    /**
     * 根据 page size 计算当前的页码。
     *
     * - [total] 列表当前的总数据量
     * - [pagingSize] 分页大小
     * - [pagingStart] 分页起始页码，0 或 1
     */
    private fun calculatePageNumber(total: Int, pagingSize: Int, pagingStart: Int): Int {
        val pageNumber = total / pagingSize + pagingStart
        Timber.d("pageStart=1, dataSize=%d, pageSize=%d, nextPageNumber=%d", total, pagingSize, pageNumber)
        return pageNumber
    }

}
