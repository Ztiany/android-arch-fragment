package com.android.base.fragment.ui

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Ztiany
 */
class AutoPaging(
    override val size: Int = defaultPagingSize,
    override val start: Int = defaultPagingStart,
) : Paging<Int>() {

    private var accumulatedPage = AtomicInteger(start)

    val current: Int
        get() = accumulatedPage.get()

    override val next: Int
        get() = current + 1


    override fun onPageRefreshed(nextPageKey: Int/* ignored */) {
        accumulatedPage.set(start)
    }

    override fun onPageAppended(nextPageKey: Int/* ignored */) {
        accumulatedPage.incrementAndGet()
    }

}