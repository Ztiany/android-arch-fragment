package com.android.base.fragment.ui

import timber.log.Timber

/**
 * @author Ztiany
 */
class AutoPaging(
    override val size: Int = defaultPagingSize,
    override val start: Int = defaultPagingStart,
    /** It is just used for debugging. You can just ignore it. */
    initialSize: Int = 0,
) : Paging() {

    private var accumulatedTotal = initialSize

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
        calculatePageNumber(accumulatedTotal, size, start)
    }

    override fun onPageRefreshed(loadedSize: Int) {
        accumulatedPage = start
        // it is only for test
        accumulatedTotal = loadedSize
        Timber.d("onPageRefreshed: accumulatedTotal = $accumulatedTotal, accumulatedPage = $accumulatedPage")
        calculatePageNumber(accumulatedTotal, size, start)
    }

}
