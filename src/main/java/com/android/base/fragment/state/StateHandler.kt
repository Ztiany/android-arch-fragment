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

/**
 * return true if the data is empty.
 */
typealias DataChecker<D> = (D) -> Boolean

internal fun <D> newDefaultChecker(): ((D) -> Boolean) {
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

/** @see handleDataState */
class StateHandlerBuilder<L, D, E> internal constructor() {
    internal var emptyChecker: DataChecker<D> = newDefaultChecker()

    internal var onEmpty: (() -> Unit)? = null
    internal var onResult: ((D) -> Unit)? = null
    internal var onError: ((error: Throwable, reason: E?) -> Unit)? = null
    internal var onLoading: ((step: L?) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    internal var monopolizedErrorHandler = false
    internal var monopolizedEmptyHandler = false
    internal var monopolizedLoadingHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        monopolizedEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (error: Throwable, reason: E?) -> Unit) {
        onError = action
        monopolizedErrorHandler = monopolized
    }

    fun onResult(action: (D) -> Unit) {
        onResult = action
    }

    fun onLoading(monopolized: Boolean = false, action: (step: L?) -> Unit) {
        onLoading = action
        monopolizedLoadingHandler = monopolized
    }

    /**
     * Set as true then the content loading layout will be shown instead of showing the refresh indicator.
     *
     * see explanation in [internalRetryByAutoRefresh].
     */
    fun showContentLoadingWhenEmpty(enable: Boolean) {
        showContentLoadingWhenEmpty = enable
    }

}

/**
 * This extension function facilitates state management for components like [BaseStateFragment] or [BaseStateDialogFragment].
 *
 * > Note: With the introduction of [handleDataState], usage of this extension is discouraged.
 *
 * Example usage:
 *
 * ```
 * class ProtocolViewModel @Inject constructor(
 *       private val repository: ProtocolRepository,
 *       savedStateHandle: SavedStateHandle
 * ) : ViewModel() {

 *       private val _protocolContentState = MutableLiveData<StateD<ProtocolData>>()
 *       val protocolContentState: LiveData<StateD<ProtocolData>> = _protocolContentState

 *       init {
 *          loadProtocolContent()
 *       }

 *       fun loadProtocolContent() {
 *              _protocolContentState.setLoading()
 *              viewModelScope.launch {
 *                      try {
 *                         _protocolContentState.setData(repository.loadProtocolContent(protocolCode))
 *                      } catch (e: XXXException) {
 *                         _protocolContentState.setError(e)
 *                      }
 *              }
 *       }
 *
 * }
 *
 * class ProtocolFragment : BaseStateFragment<ProtocolFragmentBinding>() {
 *
 *     private fun subscribeViewModel() {
 *          viewModel.protocolContentState.observe(this) {
 *             handleDataState(it) {
 *                 onResult { data ->
 *                     vb.protocolView.setProtocol(data.content)
 *                 }
 *             }
 *          }
 *      }
 *
 * }
 * ```
 */
fun <L, D, E> StateLayoutHost.handleState(
    state: State<L, D, E>,
    handler: StateHandlerBuilder<L, D, E>.() -> Unit,
) {

    val stateHandlerBuilder = StateHandlerBuilder<L, D, E>().apply(handler)

    when (state) {
        is Idle -> {}

        is Loading -> {
            if (!stateHandlerBuilder.monopolizedLoadingHandler) {
                handleLoading(stateHandlerBuilder.showContentLoadingWhenEmpty)
            }
            stateHandlerBuilder.onLoading?.invoke(state.step)
        }

        is Error -> {
            if (!stateHandlerBuilder.monopolizedErrorHandler) {
                handleError(state.error)
            }
            stateHandlerBuilder.onError?.invoke(state.error, state.reason)
        }

        is Success<D> -> {
            val data = when (state) {
                is NoData -> null
                is Data<D> -> state.value
            }
            handleData(
                data,
                stateHandlerBuilder.emptyChecker,
                stateHandlerBuilder.monopolizedEmptyHandler,
                stateHandlerBuilder.onEmpty,
                stateHandlerBuilder.onResult
            )
        }
    }
}

/** @see BaseStateFragment */
private fun StateLayoutHost.handleLoading(showContentLoadingWhenEmpty: Boolean = false) {
    if ((!isRefreshEnable) or showContentLoadingWhenEmpty && !isRefreshing()) {
        showLoadingLayout()
    } else {
        setRefreshing()
    }
}

/** @see BaseStateFragment */
private fun <D> StateLayoutHost.handleData(
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

private fun StateLayoutHost.handleError(throwable: Throwable) {
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