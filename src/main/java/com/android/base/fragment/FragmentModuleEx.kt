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

/**
 * A class used to configure the Fragment module.
 */
class FragmentModuleConfig internal constructor() {

    var defaultPageStart: Int
        set(value) {
            Paging.defaultPagingStart = value
        }
        get() {
            return Paging.defaultPagingStart
        }

    var defaultPageSize: Int
        set(value) {
            Paging.defaultPagingSize = value
        }
        get() {
            return Paging.defaultPagingSize
        }

    /**
     * Provide a default layout id for Fragments. If you don't provide a specific container id when using the related methods in Fragments, the default layout id will be used.【Must be configured】
     */
    var defaultFragmentContainerId: Int
        set(value) {
            FragmentConfig.setDefaultContainerId(value)
        }
        get() {
            return FragmentConfig.defaultContainerId()
        }

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

    /**
     * The way to load more, the default is to automatically load more when sliding to the bottom.
     */
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

    /**
     * It is used to configure the loading view.【Must be configured】
     */
    var loadingViewHostFactory: LoadingViewHostFactory?
        set(value) {
            internalLoadingViewHostFactory = value
        }
        get() = internalLoadingViewHostFactory

    /**
     * Configure it if you want to use the [Epoxy](https://github.com/airbnb/epoxy).
     */
    var epoxyLoadMoreViewFactory: EpoxyLoadMoreViewFactory
        set(value) {
            touchMe()
            internalDefaultEpoxyLoadMoreViewFactory = value
        }
        get() = internalDefaultEpoxyLoadMoreViewFactory

    /**
     * It is used to generate load more view for Paging3.
     * */
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