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

}
