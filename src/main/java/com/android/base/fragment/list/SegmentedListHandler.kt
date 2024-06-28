package com.android.base.fragment.list

import com.android.base.core.AndroidSword
import com.android.base.fragment.list.segment.BaseListFragment
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/** @see BaseListFragment */
fun ListLayoutHost<*>.handleListStartRefresh(
    showContentLoadingWhenEmpty: Boolean = !internalRetryByAutoRefresh,
) {
    if (isLoadingMore()) {
        loadMoreCompleted(true)
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
fun ListLayoutHost<*>.handleListStartLoadMore() {
    if (isRefreshing()) {
        refreshCompleted()
    }
}

/** @see [BaseListFragment] */
fun <D> ListLayoutHost<D>.handleListData(
    list: List<D>?,
    onEmpty: (HandlingProcedure.() -> Unit)? = null,
    hasMore: ((List<D>) -> Boolean)? = null,
) {
    if (isLoadingMore()) {
        if (!list.isNullOrEmpty()) {
            addData(list)
        }
    } else {
        replaceData(list ?: emptyList())
        if (isRefreshEnable && isRefreshing()) {
            refreshCompleted()
        }
    }

    if (isLoadMoreEnable) {
        if (hasMore == null) {
            loadMoreCompleted(list != null && paging.hasMore(list.size))
        } else {
            loadMoreCompleted(list != null && hasMore(list))
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
fun ListLayoutHost<*>.handleListError(throwable: Throwable) {
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

    if (isLoadMoreEnable && isLoadingMore()) {
        loadMoreFailed()
    }

    if (isEmpty()) {
        val errorTypeClassifier = AndroidSword.errorClassifier
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