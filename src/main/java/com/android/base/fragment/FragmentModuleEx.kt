package com.android.base.fragment

import com.android.base.core.AndroidSword
import com.android.base.core.AndroidSword.touchMe
import com.android.base.fragment.anim.FragmentTransitions
import com.android.base.fragment.list.epoxy.EpoxyLoadMoreViewFactory
import com.android.base.fragment.list.epoxy.internalDefaultEpoxyLoadMoreViewFactory
import com.android.base.fragment.list.paging3.PagingLoadMoreViewFactory
import com.android.base.fragment.list.paging3.internalDefaultPagingLoadMoreViewFactory
import com.android.base.fragment.tool.FragmentConfig
import com.android.base.fragment.ui.LoadingViewHostFactory
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.RefreshLoadMoreViewFactory
import com.android.base.fragment.ui.RefreshViewFactory
import com.android.base.fragment.ui.internalLoadingViewHostFactory
import com.android.base.fragment.ui.internalRetryByAutoRefresh
import com.ztiany.loadmore.adapter.LoadMode
import com.ztiany.loadmore.adapter.LoadMoreConfig

class FragmentModuleConfig internal constructor() {

    var defaultPageStart: Int
        set(value) {
            Paging.defaultPagingStart = value
        }
        get() {
            return Paging.defaultPagingStart
        }

    /** 列表分页大小。 */
    var defaultPageSize: Int
        set(value) {
            Paging.defaultPagingSize = value
        }
        get() {
            return Paging.defaultPagingSize
        }

    /** 设置一个默认的布局 id，在使用 Fragments 中相关方法时，如果没有传入特定的容器 id  时，则使用设置的默认布局 id。【必须配置】  */
    var defaultFragmentContainerId: Int
        set(value) {
            FragmentConfig.setDefaultContainerId(value)
        }
        get() {
            return FragmentConfig.defaultContainerId()
        }

    /**设置默认的 Fragment 转场动画 */
    var defaultFragmentTransitions: FragmentTransitions
        set(value) {
            FragmentConfig.setDefaultFragmentTransitions(value)
        }
        get() {
            return FragmentConfig.defaultFragmentTransitions()
        }

    var refreshViewFactory: RefreshViewFactory.Factory?
        set(value) {
            RefreshViewFactory.registerFactory(value)
        }
        get() {
            return RefreshViewFactory.getFactory()
        }

    /** 加载更多的方式，默认为滑动到底部时自动加载更多。*/
    @LoadMode
    var loadMoreMode: Int
        get() {
            return LoadMoreConfig.getLoadMode()
        }
        set(value) {
            LoadMoreConfig.setLoadMode(value)
        }

    var refreshLoadViewFactory: RefreshLoadMoreViewFactory.Factory?
        set(value) {
            RefreshLoadMoreViewFactory.registerFactory(value)
        }
        get() {
            return RefreshLoadMoreViewFactory.getFactory()
        }

    /** 用于创建 LoadingView。【必须配置】 */
    var loadingViewHostFactory: LoadingViewHostFactory?
        set(value) {
            internalLoadingViewHostFactory = value
        }
        get() = internalLoadingViewHostFactory

    /** 用于配置使用 epoxy 时，LoadMore Item 的视图 。 */
    var epoxyLoadMoreViewFactory: EpoxyLoadMoreViewFactory
        set(value) {
            touchMe()
            internalDefaultEpoxyLoadMoreViewFactory = value
        }
        get() = internalDefaultEpoxyLoadMoreViewFactory

    /** 用于配置使用 Paging3 时，LoadMore Item 的视图 。 */
    var pagingLoadMoreViewFactory: PagingLoadMoreViewFactory
        set(value) {
            touchMe()
            internalDefaultPagingLoadMoreViewFactory = value
        }
        get() = internalDefaultPagingLoadMoreViewFactory

    /**
     * set as false when your list layout is like the following.
     * when you do retrying, onRefresh will be called but the RefreshLayout will not be triggered.
     * then the SimpleMultiStateView is showing loading layout. Users can not trigger refresh by swipe.
     * This can avoid the situation that the the SimpleMultiStateView is showing loading layout
     * and the RefreshLayout is showing a refreshing state at the same time.
     *
     * ```
     * <com.android.base.fragment.widget.SimpleMultiStateView
     *     style="@style/AppStyle.SimpleMultiStateView"
     *     app:msv_viewState="loading"
     *     tools:msv_viewState="content">
     *
     *     <com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout
     *         style="@style/Widget.App.SwipeRefreshLayout"
     *         app:srl_target_id="@id/recycler_view">
     *
     *         <androidx.recyclerview.widget;.RecyclerView
     *             android:id="@+id/recycler_view"
     *             android:layout_width="match_parent"
     *             android:layout_height="match_parent"/>
     *
     *     </com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout>
     *
     * </com.android.base.fragment.widget.SimpleMultiStateView>
     * ```
     *
     * set as true when your list layout is like the following.
     * That means when you do retrying, onRefresh is triggered by the RefreshLayout.
     * Then ScrollChildSwipeRefreshLayout is never showing a loading layout.
     *
     * ```
     * <com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout
     *         style="@style/Widget.App.SwipeRefreshLayout"
     *         app:srl_target_id="@id/recycler_view">
     *
     *      <com.android.base.fragment.widget.SimpleMultiStateView
     *          style="@style/AppStyle.SimpleMultiStateView"
     *          app:msv_viewState="loading"
     *          tools:msv_viewState="content">
     *
     *         <androidx.recyclerview.widget;.RecyclerView
     *             android:id="@+id/recycler_view"
     *             android:layout_width="match_parent"
     *             android:layout_height="match_parent"/>
     *
     *      </com.android.base.fragment.widget.SimpleMultiStateView>
     *
     * </com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout>
     * ```
     */
    var retryByAutoRefresh: Boolean
        set(value) {
            internalRetryByAutoRefresh = value
        }
        get() = internalRetryByAutoRefresh

}

fun AndroidSword.fragmentModule(fragmentModuleConfig: FragmentModuleConfig.() -> Unit) {
    touchMe()
    FragmentModuleConfig().apply(fragmentModuleConfig)
}