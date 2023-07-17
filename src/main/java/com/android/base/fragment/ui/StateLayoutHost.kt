package com.android.base.fragment.ui

interface StateLayoutHost : StateLayout {

    /** trigger refreshing action and show the refreshing view. */
    fun autoRefresh()

    /** just show the refreshing view. */
    fun setRefreshing()

    /** hide the refreshing view. */
    fun refreshCompleted()

    fun isRefreshing(): Boolean

    var isRefreshEnable: Boolean

}