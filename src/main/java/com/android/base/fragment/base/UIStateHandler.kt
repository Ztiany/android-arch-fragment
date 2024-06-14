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
import com.android.base.fragment.tool.collectFlowRepeatedlyOnViewLifecycle
import com.android.base.fragment.ui.LoadingViewHost
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.observe as observeDeprecated

private fun <T> LifecycleOwner.dummyKeep(liveData: LiveData<T>) {
    liveData.observeDeprecated(this) {}
}

fun LoadingViewHost.dismissLoadingDialogDelayed(onDismiss: (() -> Unit)? = null) {
    dismissLoadingDialog(AndroidSword.minimalDialogDisplayTime, onDismiss)
}

/** Configure how to handle UI state [State]. */
class StateHandlerBuilder<L, D, E> internal constructor() {

    internal var onLoading: ((step: L?) -> Unit)? = null
    internal var onLoadingEnd: (() -> Unit)? = null
    internal var onIdle: (() -> Unit)? = null

    internal var onError: ((error: Throwable, reason: E?) -> Unit)? = null
    internal var onSuccess: ((D?) -> Unit)? = null
    internal var onData: ((D) -> Unit)? = null
    internal var onNoData: (() -> Unit)? = null

    internal var onErrorState: ((error: Throwable, reason: E?) -> Unit)? = null
    internal var onSuccessState: ((D?) -> Unit)? = null
    internal var onDataState: ((D) -> Unit)? = null
    internal var onNoDataState: (() -> Unit)? = null

    internal var loadingMessage: CharSequence = ""
    internal var showLoading: Boolean = true
    internal var forceLoading: Boolean = true
    internal var handlerErrorAsEvent: Boolean = false

    /** [onLoadingWithStep] will be called once state is [Loading]. */
    fun onLoadingWithStep(onLoading: ((step: L?) -> Unit)? = null) {
        this.onLoading = onLoading
    }

    /** [onLoading] will be called once state is [Loading]. */
    fun onLoading(onLoading: (() -> Unit)? = null) {
        onLoadingWithStep {
            onLoading?.invoke()
        }
    }

    /** [onLoadingEnd] will be called once state is not [Loading].. */
    fun onLoadingEnd(onLoadingEnd: (() -> Unit)? = null) {
        this.onLoadingEnd = onLoadingEnd
    }

    /** [onError] will be called when [State] is [Error] and is not handled. It behaves like an event. */
    fun onError(onErrorEvent: ((error: Throwable) -> Unit)? = null) {
        onErrorWithReason { error, _ -> onErrorEvent?.invoke(error) }
    }

    /** [onErrorEventWithReason] will be called when [State] is [Error] and is not handled. It behaves like an event. */
    fun onErrorWithReason(onErrorEventWithReason: ((error: Throwable, reason: E?) -> Unit)? = null) {
        this.onError = onErrorEventWithReason
    }

    /** [onErrorState] will be called once [State] is [Error]. */
    fun onErrorState(onErrorState: ((error: Throwable) -> Unit)? = null) {
        onErrorStateWithReason { error, _ -> onErrorState?.invoke(error) }
    }

    /** [onErrorStateWithReason] will be called once [State] is [Error]. */
    fun onErrorStateWithReason(onErrorStateWithReason: ((error: Throwable, reason: E?) -> Unit)? = null) {
        this.onErrorState = onErrorStateWithReason
    }

    /** [onSuccess] will always be called when [State] is [Success] and is not handled. It behaves like an event. */
    fun onSuccess(onSuccessEvent: ((D?) -> Unit)? = null) {
        this.onSuccess = onSuccessEvent
    }

    /** [onData] will be called only when [State] is [Data] and is not handled. It behaves like an event. It behaves like an event. */
    fun onData(onDataEvent: ((D) -> Unit)? = null) {
        this.onData = onDataEvent
    }

    /** [onNoData] will be called only when [State] is [NoData] and is not handled. It behaves like an event. */
    fun onNoData(onNoDataEvent: (() -> Unit)? = null) {
        this.onNoData = onNoDataEvent
    }

    /** [onSuccessState] will always be called once [State] is [Success]. */
    fun onSuccessState(onSuccessState: ((D?) -> Unit)? = null) {
        this.onSuccess = onSuccessState
    }

    /** [onDataState] will be called only when [State] is [Data]. */
    fun onDataState(onDataState: ((D) -> Unit)? = null) {
        this.onData = onDataState
    }

    /** [onDataState] will be called only when [State] is [NoData]. */
    fun onNoDataState(onNoDataState: (() -> Unit)? = null) {
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

    /** indicate whether the loading dialog is cancelable. */
    fun forceLoading() {
        this.forceLoading = true
    }

    /** indicate that the error message just displays once. */
    fun handlerErrorAsEvent() {
        handlerErrorAsEvent = true
    }

}

/**
 * 这是一个网络请求状态转换处理的通用逻辑封装，一般情况下，网络请求流程为：
 *
 * 1. 发起网络请求，展示 loading 对话框。
 * 2. 网络请求正常返回，则展示调用结果。
 * 3. 网络请求发送错误，则提示用户请求错误。
 *
 * [State] 表示请求状态，每次状态变更，[LiveData] 都应该进行通知，该方法订阅 [LiveData] 并对各种状态进行处理。
 * 展示 loading 和对错误进行提示都是自动进行的，通常情况下，只需要提供 [StateHandlerBuilder.onSuccess] 对正常的网络结果进行处理即可。
 * 当然如果希望自己处理错误，则可以提供 [StateHandlerBuilder.onError] 回调。如果希望自己处理加载中的逻辑，则可以提供 [StateHandlerBuilder.onLoading] 回调。
 *
 * 另外需要注意的是：[StateHandlerBuilder.onSuccess] =  [StateHandlerBuilder.onData] + [StateHandlerBuilder.onNoData]，请根据你的偏好进行选择。
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
    val builder = StateHandlerBuilder<L, D, E>().apply {
        handlerBuilder()
    }

    collectFlowRepeatedlyOnLifecycle(activeState, data = data) {
        handleStateInternal(it, builder)
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
    val builder = StateHandlerBuilder<L, D, E>().apply {
        handlerBuilder()
    }
    collectFlowRepeatedlyOnViewLifecycle(activeState, data = data) {
        handleStateInternal(it, builder)
    }
}

/** refers to [handleLiveData] for details. */
fun <H, L, D, E> H.handleState(
    state: State<L, D, E>,
    handlerBuilder: StateHandlerBuilder<L, D, E>.() -> Unit,
) where H : LoadingViewHost, H : LifecycleOwner {
    val builder = StateHandlerBuilder<L, D, E>().apply {
        handlerBuilder()
    }
    handleStateInternal(state, builder)
}

private fun <H, L, D, E> H.handleStateInternal(
    state: State<L, D, E>,
    handlerBuilder: StateHandlerBuilder<L, D, E>,
) where H : LoadingViewHost, H : LifecycleOwner {

    when (state) {
        is Idle -> {
            handlerBuilder.onIdle?.invoke()
        }

        //----------------------------------------loading start
        // The loading state should always be handled, so we ignore the clearAfterHanded config here.
        is Loading -> {
            if (handlerBuilder.showLoading) {
                if (handlerBuilder.onLoading == null) {
                    showLoadingDialog(handlerBuilder.loadingMessage, !handlerBuilder.forceLoading)
                } else {
                    handlerBuilder.onLoading?.invoke(state.step)
                }
            }
        }
        //----------------------------------------loading end

        //----------------------------------------error start
        is Error -> {
            dismissLoadingDialogDelayed {
                handlerBuilder.onLoadingEnd?.invoke()

                if (handlerBuilder.onError != null || handlerBuilder.onErrorState != null) {
                    if (!state.isHandled) {
                        handlerBuilder.onError?.invoke(state.error, state.reason)
                    }
                    handlerBuilder.onErrorState?.invoke(state.error, state.reason)
                } else {
                    if (!state.isHandled || (state.isHandled && !handlerBuilder.handlerErrorAsEvent)) {
                        showMessage(AndroidSword.errorConvert.convert(state.error))
                    }
                }

                state.markAsHandled()
            }
        }
        //----------------------------------------error end

        //----------------------------------------success start
        is Success<D> -> {
            dismissLoadingDialogDelayed {
                handlerBuilder.onLoadingEnd?.invoke()
                processOnSuccess(state, handlerBuilder.onSuccess, handlerBuilder.onData, handlerBuilder.onNoData, true)
                processOnSuccess(state, handlerBuilder.onSuccessState, handlerBuilder.onDataState, handlerBuilder.onNoDataState, false)
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