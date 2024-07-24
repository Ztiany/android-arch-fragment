@file:JvmName("UIKit")

package com.android.base.fragment.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.android.base.activity.collectFlowRepeatedlyOnLifecycle
import com.android.base.core.AndroidSword
import com.android.base.foundation.state.Data
import com.android.base.foundation.state.Error
import com.android.base.foundation.state.Idle
import com.android.base.foundation.state.Loading
import com.android.base.foundation.state.NoData
import com.android.base.foundation.state.State
import com.android.base.foundation.state.Success
import com.android.base.foundation.state.onData
import com.android.base.foundation.state.onError
import com.android.base.foundation.state.onLoadingWithStep
import com.android.base.foundation.state.onNoData
import com.android.base.foundation.state.onSuccess
import com.android.base.fragment.tool.HandlingProcedure
import com.android.base.fragment.tool.collectFlowRepeatedlyOnViewLifecycle
import com.android.base.fragment.tool.runRepeatedlyOnViewLifecycle
import com.android.base.fragment.ui.LoadingViewHost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.observe as observeDeprecated

private fun <T> LifecycleOwner.dummyKeep(liveData: LiveData<T>) {
    liveData.observeDeprecated(this) {}
}

fun LoadingViewHost.dismissLoadingDialogDelayed(onDismiss: (() -> Unit)? = null) {
    dismissLoadingDialog(AndroidSword.minimalDialogDisplayTime, onDismiss)
}

/** Configure how to handle UI state [State]. */
class StateHandlerBuilder<L, D, E> internal constructor() {

    internal var onLoading: (HandlingProcedure.(step: L?) -> Unit)? = null
    internal var onLoadingEnd: (() -> Unit)? = null
    internal var onIdle: (() -> Unit)? = null

    // act like an event
    internal var onError: (HandlingProcedure.(error: Throwable, reason: E?) -> Unit)? = null
    internal var onSuccess: ((D?) -> Unit)? = null
    internal var onData: ((D) -> Unit)? = null
    internal var onNoData: (() -> Unit)? = null

    // act like a state
    internal var onErrorState: (HandlingProcedure.(error: Throwable, reason: E?) -> Unit)? = null
    internal var onSuccessState: ((D?) -> Unit)? = null
    internal var onDataState: ((D) -> Unit)? = null
    internal var onNoDataState: (() -> Unit)? = null

    internal var loadingMessage: CharSequence = ""
    internal var showLoading: Boolean = true
    internal var forceLoading: Boolean = true

    /** [onLoadingWithStep] will be called once state is [Loading]. */
    fun onLoadingWithStep(onLoading: HandlingProcedure.(step: L?) -> Unit) {
        this.onLoading = onLoading
    }

    /** [onLoading] will be called once state is [Loading]. */
    fun onLoading(onLoading: () -> Unit) {
        onLoadingWithStep {
            onLoading()
        }
    }

    /** [onLoadingEnd] will be called once state is not [Loading].. */
    fun onLoadingEnd(onLoadingEnd: () -> Unit) {
        this.onLoadingEnd = onLoadingEnd
    }

    /** [onError] will be called when [State] is [Error] and is not handled. It behaves like an event. */
    fun onError(onErrorEvent: HandlingProcedure.(error: Throwable) -> Unit) {
        onErrorWithReason { error, _ ->
            onErrorEvent(error)
        }
    }

    /** [onErrorEventWithReason] will be called when [State] is [Error] and is not handled. It behaves like an event. */
    fun onErrorWithReason(onErrorEventWithReason: HandlingProcedure.(error: Throwable, reason: E?) -> Unit) {
        this.onError = onErrorEventWithReason
    }

    /** [onErrorState] will be called once [State] is [Error]. */
    fun onErrorState(onErrorState: HandlingProcedure.(error: Throwable) -> Unit) {
        onErrorStateWithReason { error, _ -> onErrorState(error) }
    }

    /** [onErrorStateWithReason] will be called once [State] is [Error]. */
    fun onErrorStateWithReason(onErrorStateWithReason: HandlingProcedure.(error: Throwable, reason: E?) -> Unit) {
        this.onErrorState = onErrorStateWithReason
    }

    /** [onSuccess] will always be called when [State] is [Success] and is not handled. It behaves like an event. */
    fun onSuccess(onSuccessEvent: (D?) -> Unit) {
        this.onSuccess = onSuccessEvent
    }

    /** [onData] will be called only when [State] is [Data] and is not handled. It behaves like an event. It behaves like an event. */
    fun onData(onDataEvent: (D) -> Unit) {
        this.onData = onDataEvent
    }

    /** [onNoData] will be called only when [State] is [NoData] and is not handled. It behaves like an event. */
    fun onNoData(onNoDataEvent: () -> Unit) {
        this.onNoData = onNoDataEvent
    }

    /** [onSuccessState] will always be called once [State] is [Success]. */
    fun onSuccessState(onSuccessState: (D?) -> Unit) {
        this.onSuccess = onSuccessState
    }

    /** [onDataState] will be called only when [State] is [Data]. */
    fun onDataState(onDataState: (D) -> Unit) {
        this.onData = onDataState
    }

    /** [onDataState] will be called only when [State] is [NoData]. */
    fun onNoDataState(onNoDataState: () -> Unit) {
        this.onNoData = onNoDataState
    }

    /** when [State] is [Loading], what to show on the loading dialog. */
    fun loadingMessage(loadingMessage: CharSequence) {
        this.loadingMessage = loadingMessage
    }

    /** indicate whether the loading dialog should be showing when [State] is [Loading]. */
    fun disableLoading() {
        showLoading = false
    }

    /** indicate whether the loading dialog is cancelable. false in default. */
    fun forceLoading(force: Boolean) {
        this.forceLoading = force
    }

}

/**
 * This function encapsulates common logic for handling network request states.
 * Typically, the network request flow includes:
 *
 * 1. Initiating the network request and displaying a loading dialog.
 * 2. Displaying the result upon successful network response.
 * 3. Prompting the user in case of a network request error.
 *
 * [State] represents the request state, and each state change should notify the associated [LiveData].
 * This method subscribes to [LiveData] and handles various states.
 * Automatic handling includes displaying loading and error prompts.
 * Usually, only [StateHandlerBuilder.onSuccess] is required to handle successful network results.
 * Optionally, you can provide [StateHandlerBuilder.onError] to handle errors yourself,
 * and [StateHandlerBuilder.onLoading] to customize loading logic.
 *
 * Additionally, note that [StateHandlerBuilder.onSuccess] = [StateHandlerBuilder.onData] + [StateHandlerBuilder.onNoData].
 * Choose based on your preference.
 */
fun <H, L, D, E> H.handleLiveData(
    data: LiveData<State<L, D, E>>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : LifecycleOwner {
    val builder = StateHandlerBuilder<L, D, E>().apply {
        handlerBuilder()
    }

    data.observe(this) { state ->
        handleStateInternal(state, builder)
    }
}

/** refers to [handleLiveData] for details. If you are using a [Fragment] with Ui, you probably need to use [handleFlowWithViewLifecycle] instead. */
fun <H, L, D, E> H.handleFlowWithLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<State<L, D, E>>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : LifecycleOwner {
    val stateHandler = StateHandlerBuilder<L, D, E>().apply(handlerBuilder)
    collectFlowRepeatedlyOnLifecycle(activeState, data = data) {
        handleStateInternal(it, stateHandler)
    }
}

/**
 * refers to [handleLiveData] for details. Notes：You should call this method in [Fragment.onViewCreated]. for more details about
 * how to collect flow from UI Layer, refers to [collectFlowRepeatedlyOnViewLifecycle].
 */
fun <H, L, D, E> H.handleFlowWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<State<L, D, E>>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : Fragment {
    val stateHandler = StateHandlerBuilder<L, D, E>().apply(handlerBuilder)
    collectFlowRepeatedlyOnViewLifecycle(activeState, data = data) {
        handleStateInternal(it, stateHandler)
    }
}

/** refers to [handleFlowWithViewLifecycle] for details. It's for handing flow data in the [runRepeatedlyOnViewLifecycle]. */
context(CoroutineScope)
fun <H, L, D, E> H.handleFlow(
    data: Flow<State<L, D, E>>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : LifecycleOwner {
    val stateHandler = StateHandlerBuilder<L, D, E>().apply(handlerBuilder)
    launch {
        data.collectLatest {
            handleStateInternal(it, stateHandler)
        }
    }
}

/** refers to [handleLiveData] for details. */
fun <H, L, D, E> H.handleState(
    state: State<L, D, E>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : LifecycleOwner {
    val stateHandler = StateHandlerBuilder<L, D, E>().apply(handlerBuilder)
    handleStateInternal(state, stateHandler)
}

private fun <H, L, D, E> H.handleStateInternal(
    state: State<L, D, E>,
    stateHandler: StateHandlerBuilder<L, D, E>,
) where H : LoadingViewHost, H : LifecycleOwner {

    when (state) {
        is Idle -> {
            stateHandler.onIdle?.invoke()
        }

        //----------------------------------------loading start
        // The loading state should always be handled, so we ignore the clearAfterHanded config here.
        is Loading -> {
            if (stateHandler.showLoading) {
                // default handling
                val defaultHandling = {
                    showLoadingDialog(stateHandler.loadingMessage, !stateHandler.forceLoading)
                    Unit
                }
                // your custom handling process
                stateHandler.onLoading?.also {
                    HandlingProcedure(defaultHandling).it(state.step)
                } ?: defaultHandling()
            }
        }
        //----------------------------------------loading end

        //----------------------------------------error start
        is Error -> {
            dismissLoadingDialogDelayed {
                stateHandler.onLoadingEnd?.invoke()

                if (stateHandler.onError != null || stateHandler.onErrorState != null) {
                    val procedure = HandlingProcedure {
                        showMessage(AndroidSword.errorConvert.convert(state.error))
                    }
                    if (!state.isHandled) {
                        stateHandler.onError?.invoke(procedure, state.error, state.reason)
                    }
                    stateHandler.onErrorState?.invoke(procedure, state.error, state.reason)
                } else if (!state.isHandled) {
                    showMessage(AndroidSword.errorConvert.convert(state.error))
                }

                state.markAsHandled()
            }
        }
        //----------------------------------------error end

        //----------------------------------------success start
        is Success<D> -> {
            dismissLoadingDialogDelayed {
                stateHandler.onLoadingEnd?.invoke()
                processOnSuccess(state, stateHandler.onSuccess, stateHandler.onData, stateHandler.onNoData, true)
                processOnSuccess(state, stateHandler.onSuccessState, stateHandler.onDataState, stateHandler.onNoDataState, false)
                state.markAsHandled()
            }
        }
        //----------------------------------------success end

    }
}

private fun <D> processOnSuccess(state: Success<D>, onSuccess: ((D?) -> Unit)?, onData: ((D) -> Unit)?, onNoData: (() -> Unit)?, asEvent: Boolean) {
    if (asEvent && state.isHandled) {
        return
    }
    when (state) {
        is NoData -> {
            onSuccess?.invoke(null)
            onNoData?.invoke()
        }

        is Data<D> -> {
            onSuccess?.invoke(state.value)
            onData?.invoke(state.value)
        }
    }
}