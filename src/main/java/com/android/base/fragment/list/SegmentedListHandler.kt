package com.android.base.fragment.list

import com.android.base.app.AndroidSword
import com.android.base.fragment.ui.ListLayoutHost

/*
We will not use State when submit list data segment by segment.

class ListHandlerBuilder<L, D, E> {

    internal var onEmpty: (() -> Unit)? = null
    internal var onList: ((List<D>) -> Unit)? = null
    internal var onError: ((error: Throwable, reason: E?) -> Unit)? = null
    internal var onLoading: ((step: L?) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = false

    internal var customErrorHandler = false
    internal var customEmptyHandler = false
    internal var customLoadingHandler = false

    internal var hasMore: ((List<D>) -> Boolean)? = null

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        customEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (error: Throwable, reason: E?) -> Unit) {
        onError = action
        customErrorHandler = monopolized
    }

    fun onList(action: (List<D>) -> Unit) {
        onList = action
    }

    fun onLoading(monopolized: Boolean = false, action: (step: L?) -> Unit) {
        onLoading = action
        customLoadingHandler = monopolized
    }

    fun useContentLoadingWhenEmpty() {
        showContentLoadingWhenEmpty = true
    }

    fun hasMore(action: (List<D>) -> Boolean) {
        hasMore = action
    }

}

fun <L, D, E> ListLayoutHost<D>.handleListState(
    state: State<L, List<D>, E>,
    builder: (ListHandlerBuilder<L, D, E>.() -> Unit)? = null,
) {
    val listStateHandler = ListHandlerBuilder<L, D, E>()
    builder?.invoke(listStateHandler)

    when (state) {
        is Idle -> {}

        is Loading -> {
            if (!listStateHandler.customLoadingHandler) {
                handleListLoading(listStateHandler.showContentLoadingWhenEmpty)
            }
            listStateHandler.onLoading?.invoke(state.step)
        }

        is Error -> {
            if (!listStateHandler.customErrorHandler) {
                handleListError(state.error)
            }
            listStateHandler.onError?.invoke(state.error, state.reason)
        }

        is Success<List<D>> -> {
            val listData = when (state) {
                is NoData -> null
                is Data<List<D>> -> state.value
            }

            val hasMoreSet = listStateHandler.hasMore
            val hasMore: (() -> Boolean)? = if (hasMoreSet != null) {
                { hasMoreSet(listData ?: emptyList()) }
            } else {
                null
            }

            handleListData(
                listData,
                listStateHandler.customEmptyHandler,
                listStateHandler.onEmpty,
                hasMore,
            )

            if (listData != null) {
                listStateHandler.onList?.invoke(listData)
            }
        }
    }
}
*/

/** @see BaseListFragment */
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
            loadMoreCompleted(list != null && getPager().hasMore(list.size))
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

/** @see BaseListFragment */
fun ListLayoutHost<*>.handleListLoading(showContentLoadingWhenEmpty: Boolean = false) {
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
