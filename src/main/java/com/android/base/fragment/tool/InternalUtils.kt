package com.android.base.fragment.tool

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.base.fragment.ui.LoadingViewHost
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@author Ztiany
 */
internal fun <T> T.dismissDialog(
    lastShowTime: Long,
    minimumMills: Long,
    onDismiss: (() -> Unit)?,
) where T : LoadingViewHost, T : LifecycleOwner {

    if (!isLoadingDialogShowing()) {
        onDismiss?.invoke()
        return
    }

    val dialogShowingTime = System.currentTimeMillis() - lastShowTime

    if (dialogShowingTime >= minimumMills) {
        dismissLoadingDialog()
        onDismiss?.invoke()
        return
    }

    lifecycleScope.launch {
        try {
            delay(minimumMills - dialogShowingTime)
            dismissLoadingDialog()
            onDismiss?.invoke()
        } catch (e: CancellationException) {
            onDismiss?.invoke()
        }
    }
}