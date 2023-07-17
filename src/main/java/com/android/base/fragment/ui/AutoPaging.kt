package com.android.base.fragment.ui

/**
 * @author Ztiany
 */
class AutoPaging(
    private val listLayoutHost: ListLayoutHost<*>,
    private val pagerSize: PagerSize,
) : Paging() {

    override val currentPage: Int
        get() = calcPageNumber(pagerSize.getSize())

    override val loadingPage: Int
        get() = if (listLayoutHost.isLoadingFirstPage()) {
            pageStart
        } else {
            currentPage + 1
        }

    override val itemCount: Int
        get() = pagerSize.getSize()

}