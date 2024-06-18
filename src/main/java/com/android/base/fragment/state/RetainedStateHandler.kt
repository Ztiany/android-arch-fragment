package com.android.base.fragment.state

import com.android.base.core.AndroidSword
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import timber.log.Timber

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
    internal var onResult: ((D) -> Unit)? = null
    internal var onError: ((error: Throwable, isEmpty: Boolean) -> Unit)? = null
    internal var onLoading: ((isEmpty: Boolean) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    internal var monopolizedErrorHandler = false
    internal var monopolizedEmptyHandler = false
    internal var monopolizedLoadingHandler = false

    fun onEmpty(monopolized: Boolean = false, action: () -> Unit) {
        onEmpty = action
        monopolizedEmptyHandler = monopolized
    }

    fun onError(monopolized: Boolean = false, action: (error: Throwable, isEmpty: Boolean) -> Unit) {
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


/**
 * Like its name "Retained State", we should handle the state in a retained way. Your code may be
 * like this:
 *
 * ```
 *@HiltViewModel
 * class TerminalListViewModel @Inject constructor(
 *     private val deviceRepository: DeviceRepository,
 *     private val dataMapper: ListVOMapper,
 * ) : ViewModel() {
 *
 *     @OptIn(ExperimentalCoroutinesApi::class)
 *     private val terminalList = selectedMerchant
 *         .flatMapMerge (1){
 *             loadTerminalList1(it.merchant)
 *         }.runningFold(SimpleDataState<List<Any>>(isRefreshing = true)) { accumulator, value ->
 *             // always dispatch the latest data
 *             if (value.data == null) {
 *                 value.copy(accumulator.data)
 *             } else value
 *         }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
 *
 *     private fun loadTerminalList1(selected: Merchant) = flow<SimpleDataState<List<Any>>> {
 *         emit(SimpleDataState(isRefreshing = true))
 *         deviceRepository.loadMerchantList(getSelectMerchantIdList(selected))
 *             .onSuccess {
 *                 emit(SimpleDataState(data = dataMapper.map(it)))
 *             }
 *             .onError {
 *                 emit(SimpleDataState(refreshError = it))
 *             }
 *     }
 *}
 * ```
 *
 * @see BaseStateFragment
 */
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
        stateHandler.onError?.invoke(it, isEmpty)
        if (stateHandler.monopolizedErrorHandler || !isEmpty) {
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