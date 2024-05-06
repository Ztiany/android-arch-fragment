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

    override val current: Int
        get() = calculatePageNumber(totalSize(), size, start)

    override val next: Int
        get() = current + 1

    override val total: Int
        get() = totalSize()

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
