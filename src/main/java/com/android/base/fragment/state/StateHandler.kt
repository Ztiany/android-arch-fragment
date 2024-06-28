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
import com.android.base.fragment.tool.HandingProcedure
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

/** @see BaseStateFragment */
class StateHandlerBuilder<L, D, E> internal constructor() {
    internal var checker: DataChecker<D> = newDefaultChecker()

    internal var onResult: (HandingProcedure.(D) -> Unit)? = null

    internal var onEmpty: (HandingProcedure.() -> Unit)? = null
    internal var onError: (HandingProcedure.(error: Throwable, reason: E?) -> Unit)? = null
    internal var onLoading: (HandingProcedure.(step: L?) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun checker(dataChecker: DataChecker<D>) {
        checker = dataChecker
    }

    fun onEmpty(action: HandingProcedure. () -> Unit) {
        onEmpty = action
    }

    fun onError(action: HandingProcedure.(error: Throwable, reason: E?) -> Unit) {
        onError = action
    }

    fun onResult(action: HandingProcedure. (D) -> Unit) {
        onResult = action
    }

    fun onLoading(action: HandingProcedure.(step: L?) -> Unit) {
        onLoading = action
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
            // default handling process
            val defaultHandling = { handleStateLoading(stateHandlerBuilder.showContentLoadingWhenEmpty) }
            // your custom handling process
            stateHandlerBuilder.onLoading?.also {
                HandingProcedure(defaultHandling).it(state.step)
            } ?: defaultHandling()
        }

        is Error -> {
            // default handling process
            val defaultHandling = { handleStateError(state.error) }
            // your custom handling process
            stateHandlerBuilder.onError?.also {
                HandingProcedure(defaultHandling).it(state.error, state.reason)
            } ?: defaultHandling()
        }

        is Success<D> -> {
            val data = when (state) {
                is NoData -> null
                is Data<D> -> state.value
            }
            handleStateData(
                data,
                stateHandlerBuilder.checker,
                stateHandlerBuilder.onEmpty,
                stateHandlerBuilder.onResult
            )
        }
    }
}

/** @see BaseStateFragment */
private fun StateLayoutHost.handleStateLoading(showContentLoadingWhenEmpty: Boolean = false) {
    if ((!isRefreshEnable) or showContentLoadingWhenEmpty && !isRefreshing()) {
        showLoadingLayout()
    } else {
        setRefreshing()
    }
}

/** @see BaseStateFragment */
private fun <D> StateLayoutHost.handleStateData(
    data: D?,
    emptyChecker: DataChecker<D> = newDefaultChecker(),
    onEmpty: (HandingProcedure.() -> Unit)? = null,
    onResult: (HandingProcedure.(D) -> Unit)? = null,
) {
    if (isRefreshEnable && isRefreshing()) {
        refreshCompleted()
    }

    if (data == null || emptyChecker(data)) {
        // default handling process
        val defaultHandling = { showEmptyLayout() }
        // your custom handling process
        onEmpty?.also {
            HandingProcedure(defaultHandling).it()
        } ?: defaultHandling()
    } else {
        // default handling process
        val defaultHandling = { showContentLayout() }
        // your custom handling process
        onResult?.also {
            HandingProcedure(defaultHandling).it(data)
        } ?: defaultHandling()
    }
}

private fun StateLayoutHost.handleStateError(throwable: Throwable) {
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