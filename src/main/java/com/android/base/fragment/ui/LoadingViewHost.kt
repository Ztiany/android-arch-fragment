package com.android.base.fragment.ui

import android.app.Dialog
import androidx.annotation.StringRes

interface Message

/**
 *  A host interface for displaying loading dialog and messages.
 *
 * @author Ztiany
 */
interface LoadingViewHost {

    /* TODO: refactor these showLoadingDialog methods, combine them into one by using a parameter with receiver. */

    /** Display a loading dialog with a default message. */
    fun showLoadingDialog(): Dialog

    /** Display a loading dialog. If the dialog is already showing, then the message will be reset. */
    fun showLoadingDialog(cancelable: Boolean): Dialog

    /** Display a loading dialog. If the dialog is already showing, then the message will be updated. */
    fun showLoadingDialog(message: CharSequence, cancelable: Boolean): Dialog

    /** Display a loading dialog. If the dialog is already showing, then the message will be updated. */
    fun showLoadingDialog(@StringRes messageId: Int, cancelable: Boolean): Dialog

    fun dismissLoadingDialog()

    fun dismissLoadingDialog(minimumMills: Long, onDismiss: (() -> Unit)? = null)

    fun isLoadingDialogShowing(): Boolean

    /* TODO: refactor these showMessage methods, combine them into one by using a Message object as parameter. */

    fun showMessage(message: CharSequence)

    fun showMessage(@StringRes messageId: Int)

    fun showMessage(message: Message)

}