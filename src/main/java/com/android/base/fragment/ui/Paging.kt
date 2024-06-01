package com.android.base.fragment.ui


/**
 * @author Ztiany
 */
abstract class Paging {

    /** Load a list from the start position. */
    abstract val start: Int

    /** the size of each page. */
    abstract val size: Int

    /** accumulated page size. Note: If your items can be removed form your list. this number don't equals your real item counts.  */
    abstract val total: Int

    /** current page number */
    abstract val current: Int

    /** next  page number */
    abstract val next: Int

    abstract fun onPageAppended(appendedSize: Int)

    abstract fun onPageRefreshed(loadedSize: Int)

    fun hasMore(loadedSize: Int): Boolean {
        return loadedSize >= this.size
    }

    fun getLoadingPage(isRefresh: Boolean): Int {
        return if (isRefresh) {
            current
        } else {
            next
        }
    }

    companion object {
        var defaultPagingStart = 0
        var defaultPagingSize = 20
    }

}