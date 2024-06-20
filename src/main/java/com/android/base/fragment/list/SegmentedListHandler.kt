package com.android.base.fragment.list

import com.android.base.core.AndroidSword
import com.android.base.fragment.list.segment.BaseListFragment
import com.android.base.fragment.ui.ListLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/** @see BaseListFragment */
fun ListLayoutHost<*>.handleListLoading(
    cancelRefreshWhenLoadingMore: Boolean = true,
    showContentLoadingWhenEmpty: Boolean = !internalRetryByAutoRefresh,
) {
    if (isLoadingMore()) {
        if (cancelRefreshWhenLoadingMore) {
            refreshCompleted()
        }
        return
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

/** @see [BaseListFragment] */
fun <D> ListLayoutHost<D>.handleListData(
    list: List<D>?,
    monopolizedEmptyHandler: Boolean = false,
    onEmpty: (() -> Unit)? = null,
    hasMore: (() -> Boolean)? = null,
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
            loadMoreCompleted(hasMore())
        }
    }

    if (isEmpty()) {
        if (!monopolizedEmptyHandler) {
            showEmptyLayout()
        }
        onEmpty?.invoke()
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