package com.android.base.fragment.state

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.R
import com.android.base.fragment.base.BaseUIFragment
import com.android.base.fragment.state.BaseStateFragment.Companion.CONTENT
import com.android.base.fragment.state.BaseStateFragment.Companion.EMPTY
import com.android.base.fragment.state.BaseStateFragment.Companion.ERROR
import com.android.base.fragment.state.BaseStateFragment.Companion.LOADING
import com.android.base.fragment.ui.CommonId
import com.android.base.fragment.ui.RefreshView
import com.android.base.fragment.ui.StateLayoutConfig
import com.android.base.fragment.ui.StateLayoutConfig.RetryableState
import com.android.base.fragment.ui.StateLayoutHost

/**
 * 1. 支持显示 [CONTENT], [LOADING], [ERROR], [EMPTY] 等状态布局、支持下拉刷新。
 * 2. 使用的布局中必须有一个 id 为 [R.id.base_state_layout] 的 Layout，确保 Layout 实现了 [com.android.base.fragment.ui.StateLayout]。
 * 3. [RefreshView] (下拉刷新) 的视图 id 必须设置为  [R.id.base_refresh_layout]，没有添加则表示不需要下拉刷新功能。
 * 4. 默认所有重试和下拉刷新都会调用 [onRefresh]，子类可以修改该行为。
 *
 * 可以使用 [handleSateResource] 来处理加载到的数据。比如：
 *
 * ```kotlin
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
 *             handleSateResource(it) {
 *                 onResult { data ->
 *                     vb.protocolView.setProtocol(data.content)
 *                 }
 *             }
 *          }
 *      }
 *
 * }
 * ```
 *
 * @author Ztiany
 * Date :   2016-03-19 23:09
 */
abstract class BaseStateFragment<VB : ViewBinding> : BaseUIFragment<VB>(), StateLayoutHost {

    private lateinit var stateLayoutHostImpl: StateLayoutHost

    override fun internalOnSetUpCreatedView(view: View, savedInstanceState: Bundle?) {
        stateLayoutHostImpl = buildStateLayoutHost(
            view.findViewById(CommonId.STATE_ID),
            view.findViewById(CommonId.REFRESH_ID)
        ) {
            this.onRefresh = {
                this@BaseStateFragment.onRefresh()
            }
            this.onRetry = {
                this@BaseStateFragment.onRetry(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshCompleted()
    }

    protected open fun onRetry(@RetryableState state: Int) {
        if (stateLayoutHostImpl.isRefreshEnable) {
            if (!isRefreshing()) {
                stateLayoutHostImpl.autoRefresh()
            }
        } else {
            onRefresh()
        }
    }

    protected open fun onRefresh() {}

    override var isRefreshEnable: Boolean
        get() = stateLayoutHostImpl.isRefreshEnable
        set(value) {
            stateLayoutHostImpl.isRefreshEnable = value
        }

    override fun getStateLayoutConfig(): StateLayoutConfig = stateLayoutHostImpl.stateLayoutConfig

    override fun setRefreshing() {
        stateLayoutHostImpl.setRefreshing()
    }

    override fun isRefreshing(): Boolean {
        return stateLayoutHostImpl.isRefreshing()
    }

    override fun refreshCompleted() = stateLayoutHostImpl.refreshCompleted()

    override fun autoRefresh() = stateLayoutHostImpl.autoRefresh()

    override fun showContentLayout() = stateLayoutHostImpl.showContentLayout()

    override fun showLoadingLayout() = stateLayoutHostImpl.showLoadingLayout()

    override fun showEmptyLayout() = stateLayoutHostImpl.showEmptyLayout()

    override fun showErrorLayout() = stateLayoutHostImpl.showErrorLayout()

    override fun showRequesting() = stateLayoutHostImpl.showRequesting()

    override fun showBlank() = stateLayoutHostImpl.showBlank()

    override fun showNetErrorLayout() = stateLayoutHostImpl.showNetErrorLayout()

    override fun showServerErrorLayout() = stateLayoutHostImpl.showServerErrorLayout()

    override fun currentStatus() = stateLayoutHostImpl.currentStatus()

    @Suppress("UNUSED")
    companion object {
        const val CONTENT = StateLayoutConfig.CONTENT
        const val LOADING = StateLayoutConfig.LOADING
        const val ERROR = StateLayoutConfig.ERROR
        const val EMPTY = StateLayoutConfig.EMPTY
        const val NET_ERROR = StateLayoutConfig.NET_ERROR
        const val SERVER_ERROR = StateLayoutConfig.SERVER_ERROR
    }

}