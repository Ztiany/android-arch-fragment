package com.android.base.fragment.list.epoxy

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.android.base.fragment.ui.ListLayoutHost
import kotlinx.coroutines.flow.Flow

/* TODO: implement it. */
data class AdvancedListState<L, D, E>(
    /** 列表数据 */
    val data: List<D> = emptyList(),

    // 刷新状态
    val isRefreshing: Boolean = false,
    val refreshError: Throwable? = null,

    // 加载更多状态
    val isLoadingMore: Boolean = false,
    val loadMoreError: Throwable? = null,
    val hasMore: Boolean = false,
)

class AdvancedListStateHandlerBuilder {

}

fun <H, L, D, E> H.handleListStateWithViewLifecycle(
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    data: Flow<AdvancedListState<L, D, E>>,
) where H : ListLayoutHost<D>, H : Fragment {

}