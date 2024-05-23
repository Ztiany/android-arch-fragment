package com.android.base.fragment.ui

/**
 * set as false when your list layout is like this:
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
 * set as true when your list layout is like this:
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
internal var internalRetryByAutoRefresh: Boolean = false