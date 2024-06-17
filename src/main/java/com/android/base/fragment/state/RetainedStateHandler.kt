package com.android.base.fragment.state

import com.android.base.core.AndroidSword
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

interface DataState<T> {
    val data: T?
    val isRefreshing: Boolean
    val refreshError: Throwable?
}

data class SimpleDataState<T>(
    override val data: T? = null,
    override val isRefreshing: Boolean = false,
    override val refreshError: Throwable? = null,
) : DataState<T>

/** @see BaseStateFragment */
class DataStateHandlerBuilder<D> internal constructor() {
    internal var emptyChecker: DataChecker<D> = newDefaultChecker()

    internal var onEmpty: (() -> Unit)? = null
    internal var onResult: ((data: D) -> Unit)? = null
    internal var onError: ((error: Throwable) -> Unit)? = null
    internal var onLoading: ((isEmpty: Boolean) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    internal var monopolizedErrorHandler = false
    internal var monopolizedEmptyHandler = false
    internal var monopolizedLoadingHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        monopolizedEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (error: Throwable) -> Unit) {
        onError = action
        monopolizedErrorHandler = monopolized
    }

    fun onResult(action: (D) -> Unit) {
        onResult = action
    }

    fun onLoading(monopolized: Boolean = false, action: (isEmpty: Boolean) -> Unit) {
        onLoading = action
        monopolizedLoadingHandler = monopolized
    }

    fun useContentLoadingWhenEmpty() {
        showContentLoadingWhenEmpty = true
    }

}

/** @see BaseStateFragment */
fun <D> StateLayoutHost.handleDataState(
    state: DataState<D>,
    handler: DataStateHandlerBuilder<D>.() -> Unit,
) {

    val stateHandler = DataStateHandlerBuilder<D>().apply(handler)

    val data = state.data
    val dispatchData = {
        data?.let {
            stateHandler.onResult?.invoke(it)
            showContentLayout()
        }
    }
    val isEmpty = data == null || stateHandler.emptyChecker(data)
    val error = state.refreshError

    // handing on refreshing.
    if (state.isRefreshing) {
        if (!stateHandler.monopolizedLoadingHandler) {
            if ((!isRefreshEnable) or isEmpty && stateHandler.showContentLoadingWhenEmpty && !isRefreshing()) {
                showLoadingLayout()
            } else {
                setRefreshing()
            }
        }

        stateHandler.onLoading?.invoke(isEmpty)
        // always dispatch data if we have.
        dispatchData()
        return
    }

    // not refreshing, dismiss the refresh indicator.
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

    // always dispatch data if we have.
    dispatchData()

    // refreshing successfully but has no data.
    if (error == null && isEmpty) {
        if (!stateHandler.monopolizedEmptyHandler) {
            showEmptyLayout()
        }
        stateHandler.onEmpty?.invoke()
        return
    }

    error?.let {
        // always dispatch error.
        stateHandler.onError?.invoke(it)
        if (stateHandler.monopolizedErrorHandler && !isEmpty) {
            return
        }

        // refreshing failed but we don't have previously loaded data.
        val errorTypeClassifier = AndroidSword.errorClassifier
        if (errorTypeClassifier != null) {
            when {
                errorTypeClassifier.isNetworkError(it) -> showNetErrorLayout()
                errorTypeClassifier.isServerError(it) -> showServerErrorLayout()
                else -> showErrorLayout()
            }
        } else {
            showErrorLayout()
        }
    }
}