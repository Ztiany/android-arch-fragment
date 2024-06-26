package com.android.base.fragment.ui

import timber.log.Timber

/**
 * 根据 page size 计算当前的页码。
 *
 * - [total] 列表当前的总数据量。
 * - [pagingSize] 分页大小。
 * - [pagingStart] 分页起始页码，必须是 0 或 1。
 */
fun calculatePageNumber(total: Int, pagingSize: Int, pagingStart: Int): Int {
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
    Timber.d(
        "calculatePageNumber: total=%d, start=%d, size=%d, current=%d, next=%d",
        total,
        pagingStart,
        pagingSize,
        pageNumber,
        pageNumber + 1
    )
    return pageNumber
}

fun calculateNextPageNumber(total: Int, pagingSize: Int, pagingStart: Int): Int {
    return calculatePageNumber(total, pagingSize, pagingStart) + 1
}