package com.android.base.fragment.ui

/**
 * An interface for a view that supports refresh.
 *
 * @author Ztiany
 */
interface RefreshView {

    /** trigger refreshing action and show the refreshing view. */
    fun autoRefresh()

    /** just show the refreshing view. */
    fun setRefreshing()

    /** hide the refreshing view. */
    fun refreshCompleted()

    fun setRefreshHandler(refreshHandler: RefreshHandler?)

    fun isRefreshing(): Boolean

    var isRefreshEnable: Boolean

    abstract class RefreshHandler {

        open fun canRefresh(): Boolean {
            return true
        }

        abstract fun onRefresh()
    }

}