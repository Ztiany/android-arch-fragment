package com.android.base.fragment.state

import androidx.lifecycle.ViewModel
import com.android.base.core.AndroidSword
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.ui.StateLayoutHost
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    internal var checker: DataChecker<D> = newDefaultChecker()

    internal var onResult: (suspend (D) -> Unit)? = null

    internal var onEmpty: (suspend HandlingProcedure.() -> Unit)? = null
    internal var onError: (suspend HandlingProcedure.(error: Throwable, isEmpty: Boolean) -> Unit)? = null
    internal var onLoading: (suspend HandlingProcedure.(isEmpty: Boolean) -> Unit)? = null

    internal var showContentLoadingWhenEmpty = !internalRetryByAutoRefresh

    fun checker(dataChecker: DataChecker<D>) {
        checker = dataChecker
    }

    fun onEmpty(action: suspend HandlingProcedure. () -> Unit) {
        onEmpty = action
    }

    fun onError(action: suspend HandlingProcedure.(error: Throwable, isEmpty: Boolean) -> Unit) {
        onError = action
    }

    fun onResult(action: suspend (D) -> Unit) {
        onResult = action
    }

    fun onLoading(action: suspend HandlingProcedure.(isEmpty: Boolean) -> Unit) {
        onLoading = action
    }

    fun useContentLoadingWhenEmpty() {
        showContentLoadingWhenEmpty = true
    }

}


/**
 * Like its name "Retained State", we should handle the state in a retained way. Your code in your [ViewModel] may be
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
 *         }.scan(SimpleDataState<List<Any>>(isRefreshing = true)) { accumulator, value ->
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
 * then handle data in your [BaseStateFragment] or [BaseStateDialogFragment]:
 *
 * ```
 * class ArticleFragment : BaseStateFragment<ArticleFragmentBinding>() {
 *
 *    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) = runRepeatedlyOnViewLifecycle {
 *         stateLayoutController.handleFlowDataState(viewModel.articles) {
 *             onResult { list ->
 *                 articleAdapter.replaceAll(list)
 *             }
 *         }
 *     }
 *
 * }
 * ```
 *
 * @see BaseStateFragment
 */
context(CoroutineScope)
fun <D> StateLayoutHost.handleFlowDataState(
    flowData: Flow<DataState<D>>,
    handler: DataStateHandlerBuilder<D>.() -> Unit,
) {
    launch {
        flowData.collectLatest {
            handleDataState(it, handler)
        }
    }
}

suspend fun <D> StateLayoutHost.handleDataState(
    state: DataState<D>,
    handler: DataStateHandlerBuilder<D>.() -> Unit,
) {
    val stateHandler = DataStateHandlerBuilder<D>().apply(handler)

    val data = state.data
    val dispatchData = suspend {
        data?.let {
            stateHandler.onResult?.invoke(it)
            showContentLayout()
        }
    }

    val isEmpty = data == null || stateHandler.checker(data)
    val error = state.refreshError

    // handling on refreshing.
    if (state.isRefreshing) {
        // default handling process
        val defaultHandling = {
            if ((!isRefreshEnable) or isEmpty && stateHandler.showContentLoadingWhenEmpty && !isRefreshing()) {
                showLoadingLayout()
            } else {
                setRefreshing()
            }
        }
        // your custom handling process
        stateHandler.onLoading?.also {
            HandlingProcedure(defaultHandling).it(isEmpty)
        } ?: defaultHandling()
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
        // default handling process
        val defaultHandling = { showEmptyLayout() }
        // your custom handling process
        stateHandler.onEmpty?.also {
            HandlingProcedure(defaultHandling).it()
        } ?: defaultHandling()
        return
    }

    error?.let {
        // default handling process
        val defaultHandling = {
            // refreshing failed but we don't have previously loaded data.
            val errorTypeClassifier = AndroidSword.requestErrorClassifier
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
        // your custom handling process
        stateHandler.onError?.also {
            // always dispatch error.
            HandlingProcedure(defaultHandling).it(error, isEmpty)
        } ?: defaultHandling()
    }
}