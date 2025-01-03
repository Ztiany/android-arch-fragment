package com.android.base.fragment.list

import com.android.base.core.AndroidSword
import com.android.base.fragment.list.segment.BaseListFragment
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.SegmentedListLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/** @see BaseListFragment */
fun SegmentedListLayoutHost<*, *>.handleListStartRefresh(
    showContentLoadingWhenEmpty: Boolean = !internalRetryByAutoRefresh,
) {
    if (isLoadingMore()) {
        loadMoreCompleted(hasMore = true, appended = false)
    }
    if (isEmpty()) {
        if ((!isRefreshEnable) or showContentLoadingWhenEmpty && !isRefreshing()) {
            showLoadingLayout()
        } else {
            setRefreshing()
        }
    } else {
        setRefreshing()
    }
}

/** @see BaseListFragment */
fun SegmentedListLayoutHost<*, *>.handleListStartLoadMore() {
    if (isRefreshing()) {
        refreshCompleted()
    }
}

/** @see [BaseListFragment] */
fun <D, Key : Any> SegmentedListLayoutHost<D, Key>.handleListData(
    list: List<D>?,
    nextPageKey: Key,
    onEmpty: (HandlingProcedure.() -> Unit)? = null,
    hasMore: ((List<D>) -> Boolean)? = null,
) {
    if (isLoadingMore()) {
        if (!list.isNullOrEmpty()) {
            addData(list)
            paging.onPageAppended(nextPageKey)
        }
    } else {
        replaceData(list ?: emptyList())
        if (isRefreshEnable && isRefreshing()) {
            refreshCompleted()
            paging.onPageRefreshed(nextPageKey)
        }
    }

    if (isLoadMoreEnable) {
        if (hasMore == null) {
            loadMoreCompleted(list != null && paging.hasMore(list.size), appended = !list.isNullOrEmpty())
        } else {
            loadMoreCompleted(list != null && hasMore(list), appended = !list.isNullOrEmpty())
        }
    }

    if (isEmpty()) {
        // default handling process
        val defaultHandling = { showEmptyLayout() }
        // your custom handling process
        onEmpty?.also {
            HandlingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    } else {
        showContentLayout()
    }
}

/** @see [BaseListFragment] */
fun <D> SegmentedListLayoutHost<D, Int>.handleListData(
    list: List<D>?,
    onEmpty: (HandlingProcedure.() -> Unit)? = null,
    hasMore: ((List<D>) -> Boolean)? = null,
) {
    if (isLoadingMore()) {
        if (!list.isNullOrEmpty()) {
            addData(list)
            paging.onPageAppended(list.size)
        }
    } else {
        replaceData(list ?: emptyList())
        if (isRefreshEnable && isRefreshing()) {
            refreshCompleted()
            paging.onPageRefreshed(
                /* We pass the loaded list size as the key, but for [AutoPaging], this parameter will just be ignored. */
                list?.size ?: 0
            )
        }
    }

    if (isLoadMoreEnable) {
        if (hasMore == null) {
            loadMoreCompleted(list != null && paging.hasMore(list.size), appended = !list.isNullOrEmpty())
        } else {
            loadMoreCompleted(list != null && hasMore(list), appended = !list.isNullOrEmpty())
        }
    }

    if (isEmpty()) {
        // default handling process
        val defaultHandling = { showEmptyLayout() }
        // your custom handling process
        onEmpty?.also {
            HandlingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    } else {
        showContentLayout()
    }
}

/** @see BaseListFragment */
fun SegmentedListLayoutHost<*, *>.handleListError(throwable: Throwable) {
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

    if (isLoadMoreEnable && isLoadingMore()) {
        loadMoreFailed()
    }

    if (isEmpty()) {
        val errorTypeClassifier = AndroidSword.requestErrorClassifier
        if (errorTypeClassifier != null) {
            when {
                errorTypeClassifier.isNetworkError(throwable) -> showNetErrorLayout()
                errorTypeClassifier.isServerError(throwable) -> showServerErrorLayout()
                else -> showErrorLayout()
            }
        } else {
            showErrorLayout()
        }
    } else {
        showContentLayout()
    }
}