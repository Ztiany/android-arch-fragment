package com.android.base.fragment.ui


/**
 * @author Ztiany
 */
abstract class Paging<Key : Any> {

    /** Load a list from the start position. */
    abstract val start: Key

    /** the size of each page. */
    abstract val size: Int

    /** next  page number */
    abstract val next: Key

    abstract fun onPageAppended(nextPageKey: Key)

    abstract fun onPageRefreshed(nextPageKey: Key)

    open fun hasMore(loadedSize: Int): Boolean {
        return loadedSize >= this.size
    }

    companion object {
        var defaultPagingStart = 0
        var defaultPagingSize = 20
    }

}