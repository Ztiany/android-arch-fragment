package com.android.base.fragment.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatDialogFragment
import com.android.base.activity.OnBackPressListener
import com.android.base.activity.fragmentHandleBackPress
import com.android.base.delegate.State
import com.android.base.delegate.fragment.FragmentDelegate
import com.android.base.delegate.fragment.FragmentDelegateOwner
import com.android.base.delegate.helper.FragmentDelegates
import com.android.base.fragment.tool.dismissDialog
import com.android.base.fragment.ui.LoadingViewHost
import com.android.base.fragment.ui.Message
import com.android.base.fragment.ui.internalLoadingViewHostFactory
import timber.log.Timber

/**
 * @author Ztiany
 * @see BaseFragment
 */
open class BaseDialogFragment : AppCompatDialogFragment(), OnBackPressListener, FragmentDelegateOwner, LoadingViewHost {

    private var recentShowingDialogTime: Long = 0

    private var loadingViewHost: LoadingViewHost? = null

    private val fragmentDelegates by lazy { FragmentDelegates(this) }

    private fun tag() = this.javaClass.simpleName

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.tag(tag()).d("onAttach() called with: context = [$context]")
        fragmentDelegates.callOnAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(tag()).d("-->onCreate  savedInstanceState  =  $savedInstanceState")
        fragmentDelegates.callOnCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Timber.tag(tag()).d("-->onCreateView  savedInstanceState = %s", savedInstanceState)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag(tag()).d("-->onViewCreated  savedInstanceState = %s", savedInstanceState)
        fragmentDelegates.callOnViewCreated(view, savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.tag(tag()).d("-->onActivityCreated savedInstanceState  =  $savedInstanceState")
        fragmentDelegates.callOnActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Timber.tag(tag()).d("-->onStart")
        fragmentDelegates.callOnStart()
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(tag()).d("-->onResume")
        fragmentDelegates.callOnResume()
    }

    override fun onPause() {
        Timber.tag(tag()).d("-->onPause")
        fragmentDelegates.callOnPause()
        super.onPause()
    }

    override fun onStop() {
        Timber.tag(tag()).d("-->onStop")
        fragmentDelegates.callOnStop()
        super.onStop()
    }

    override fun onDestroyView() {
        Timber.tag(tag()).d("-->onDestroyView")
        fragmentDelegates.callOnDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        Timber.tag(tag()).d("-->onDestroy")
        fragmentDelegates.callOnDestroy()
        dismissLoadingDialog()
        super.onDestroy()
    }

    override fun onDetach() {
        Timber.tag(tag()).d("-->onDetach")
        fragmentDelegates.callOnDetach()
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        fragmentDelegates.callOnSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Timber.tag(tag()).d("-->setUserVisibleHint = $isVisibleToUser")
        fragmentDelegates.callSetUserVisibleHint(isVisibleToUser)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.tag(tag()).d("-->onHiddenChanged = $hidden")
        fragmentDelegates.callOnHiddenChanged(hidden)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        fragmentDelegates.callOnRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragmentDelegates.callOnActivityResult(requestCode, resultCode, data)
    }

    @UiThread
    override fun addDelegate(fragmentDelegate: FragmentDelegate<*>) {
        fragmentDelegates.addDelegate(fragmentDelegate)
    }

    @UiThread
    override fun removeDelegate(fragmentDelegate: FragmentDelegate<*>): Boolean {
        return fragmentDelegates.removeDelegate(fragmentDelegate)
    }

    @UiThread
    override fun removeDelegateWhile(predicate: (FragmentDelegate<*>) -> Boolean) {
        fragmentDelegates.removeDelegateWhile(predicate)
    }

    @UiThread
    override fun findDelegate(predicate: (FragmentDelegate<*>) -> Boolean): FragmentDelegate<*>? {
        return fragmentDelegates.findDelegate(predicate)
    }

    override fun getCurrentState(): State {
        return fragmentDelegates.getCurrentState()
    }

    final override fun onBackPressed(): Boolean {
        return handleBackPress() || fragmentHandleBackPress(this)
    }

    protected open fun handleBackPress(): Boolean {
        return false
    }

    protected open fun onCreateLoadingView(): LoadingViewHost? {
        return null
    }

    private fun loadingView(): LoadingViewHost {
        val loadingViewImpl = loadingViewHost
        return if (loadingViewImpl != null) {
            loadingViewImpl
        } else {
            loadingViewHost = onCreateLoadingView() ?: internalLoadingViewHostFactory?.invoke(requireContext())
            loadingViewHost ?: throw NullPointerException("you need to config LoadingViewFactory in Sword or implement onCreateLoadingView.")
        }
    }

    override fun showLoadingDialog(): Dialog {
        recentShowingDialogTime = System.currentTimeMillis()
        return loadingView().showLoadingDialog(true)
    }

    override fun showLoadingDialog(cancelable: Boolean): Dialog {
        recentShowingDialogTime = System.currentTimeMillis()
        return loadingView().showLoadingDialog(cancelable)
    }

    override fun showLoadingDialog(message: CharSequence, cancelable: Boolean): Dialog {
        recentShowingDialogTime = System.currentTimeMillis()
        return loadingView().showLoadingDialog(message, cancelable)
    }

    override fun showLoadingDialog(@StringRes messageId: Int, cancelable: Boolean): Dialog {
        recentShowingDialogTime = System.currentTimeMillis()
        return loadingView().showLoadingDialog(messageId, cancelable)
    }

    override fun dismissLoadingDialog() {
        loadingViewHost?.dismissLoadingDialog()
    }

    override fun dismissLoadingDialog(minimumMills: Long, onDismiss: (() -> Unit)?) {
        dismissDialog(recentShowingDialogTime, minimumMills, onDismiss)
    }

    override fun isLoadingDialogShowing(): Boolean {
        return loadingViewHost != null && loadingView().isLoadingDialogShowing()
    }

    override fun showMessage(message: CharSequence) {
        loadingView().showMessage(message)
    }

    override fun showMessage(@StringRes messageId: Int) {
        loadingView().showMessage(messageId)
    }

    override fun showMessage(message: Message) {
        loadingView().showMessage(message)
    }

}