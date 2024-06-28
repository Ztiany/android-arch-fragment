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
    /** callback for providing the list's size which will be used to calculate the page number. */
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
        accumulatedPage++
        // it is only for test
        accumulatedTotal += appendedSize
        Timber.d("onPageAppended: accumulatedTotal = $accumulatedTotal, accumulatedPage = $accumulatedPage")
        calculatePageNumber(totalSize(), size, start)
    }

    override fun onPageRefreshed(loadedSize: Int) {
        accumulatedPage = start
        // it is only for test
        accumulatedTotal = size
        Timber.d("onPageRefreshed: accumulatedTotal = $accumulatedTotal, accumulatedPage = $accumulatedPage")
        calculatePageNumber(totalSize(), size, start)
    }

}
