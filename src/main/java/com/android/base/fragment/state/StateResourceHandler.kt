package com.android.base.fragment.state

import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import android.util.SparseLongArray
import androidx.core.util.isEmpty
import com.android.base.core.AndroidSword
import com.android.base.foundation.state.Data
import com.android.base.foundation.state.Error
import com.android.base.foundation.state.Idle
import com.android.base.foundation.state.Loading
import com.android.base.foundation.state.NoData
import com.android.base.foundation.state.State
import com.android.base.foundation.state.Success
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh

/** @see BaseStateFragment */
typealias DataChecker<D> = (D) -> Boolean

private fun <D> newDefaultChecker(): ((D) -> Boolean) {
    return { data ->
        (data is CharSequence && (data.isEmpty() || data.isBlank()))
                || (data is Collection<*> && data.isEmpty())
                || (data is Map<*, *> && data.isEmpty())
                || (data is SparseArray<*> && data.isEmpty())
                || (data is SparseIntArray && data.isEmpty())
                || (data is SparseLongArray && data.isEmpty())
                || (data is SparseBooleanArray && data.isEmpty())
                || (data is Array<*> && data.isEmpty())
                || (data is IntArray && data.isEmpty())
                || (data is LongArray && data.isEmpty())
                || (data is BooleanArray && data.isEmpty())
    }
}

/** @see BaseStateFragment */
class StateHandlerBuilder<L, D, E> internal constructor(){
    internal var emptyChecker: DataChecker<D> = newDefaultChecker()

    internal var onEmpty: (() -> Unit)? = null
    internal var onResult: ((D) -> Unit)? = null
    internal var onError: ((error: Throwable, reason: E?) -> Unit)? = null
    internal var onLoading: ((step: L?) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    internal var customErrorHandler = false
    internal var customEmptyHandler = false
    internal var customLoadingHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        customEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (error: Throwable, reason: E?) -> Unit) {
        onError = action
        customErrorHandler = monopolized
    }

    fun onResult(action: (D) -> Unit) {
        onResult = action
    }

    fun onLoading(monopolized: Boolean = false, action: (step: L?) -> Unit) {
        onLoading = action
        customLoadingHandler = monopolized
    }

    fun useContentLoadingWhenEmpty() {
        showContentLoadingWhenEmpty = true
    }

}

/** @see BaseStateFragment */
fun <L, D, E> StateLayoutHost.handleMultiState(
    state: State<L, D, E>,
    handler: StateHandlerBuilder<L, D, E>.() -> Unit,
) {

    val stateHandlerBuilder = StateHandlerBuilder<L, D, E>().apply(handler)

    when (state) {
        is Idle -> {}

        is Loading -> {
            if (!stateHandlerBuilder.customLoadingHandler) {
                handleStateLoading(stateHandlerBuilder.showContentLoadingWhenEmpty)
            }
            stateHandlerBuilder.onLoading?.invoke(state.step)
        }

        is Error -> {
            if (!stateHandlerBuilder.customErrorHandler) {
                handleStateError(state.error)
            }
            stateHandlerBuilder.onError?.invoke(state.error, state.reason)
        }

        is Success<D> -> {
            val data = when (state) {
                is NoData -> null
                is Data<D> -> state.value
            }
            handleStateData(
                data,
                stateHandlerBuilder.emptyChecker,
                stateHandlerBuilder.customEmptyHandler,
                stateHandlerBuilder.onEmpty,
                stateHandlerBuilder.onResult
            )
        }
    }
}

/** @see BaseStateFragment */
fun StateLayoutHost.handleStateLoading(showContentLoadingWhenEmpty: Boolean = false) {
    if ((!isRefreshEnable) or showContentLoadingWhenEmpty && !isRefreshing()) {
        showLoadingLayout()
    } else {
        setRefreshing()
    }
}

/** @see BaseStateFragment */
fun <D> StateLayoutHost.handleStateData(
    data: D?,
    emptyChecker: DataChecker<D> = newDefaultChecker(),
    onEmpty: (() -> Unit)? = null,
    onResult: ((D) -> Unit)? = null,
) {
    handleStateData(data, emptyChecker, false, onEmpty, onResult)
}

/** @see BaseStateFragment */
private fun <D> StateLayoutHost.handleStateData(
    data: D?,
    emptyChecker: DataChecker<D> = newDefaultChecker(),
    monopolizedEmptyHandler: Boolean = false,
    onEmpty: (() -> Unit)? = null,
    onResult: ((D) -> Unit)? = null,
) {
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

    if (data == null || emptyChecker(data)) {
        if (!monopolizedEmptyHandler) {
            showEmptyLayout()
        }
        onEmpty?.invoke()
    } else {
        onResult?.invoke(data)
        showContentLayout()
    }
}

fun StateLayoutHost.handleStateError(throwable: Throwable) {
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

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
}