package com.android.base.fragment

import com.android.base.core.AndroidSword
import com.android.base.fragment.anim.FragmentAnimator
import com.android.base.fragment.epoxy.EpoxyLoadMoreViewFactory
import com.android.base.fragment.epoxy.internalDefaultEpoxyLoadMoreViewFactory
import com.android.base.fragment.tool.FragmentConfig
import com.android.base.fragment.ui.LoadingViewHostFactory
import com.android.base.fragment.ui.Paging
import com.android.base.fragment.ui.RefreshLoadMoreViewFactory
import com.android.base.fragment.ui.RefreshViewFactory
import com.android.base.fragment.ui.internalLoadingViewHostFactory
import com.ztiany.loadmore.adapter.LoadMode
import com.ztiany.loadmore.adapter.LoadMoreConfig

/** 列表分页起始页。 */
var AndroidSword.defaultPageStart: Int
    set(value) {
        touch()
        Paging.defaultPagingStart = value
    }
    get() {
        return Paging.defaultPagingStart
    }

/** 列表分页大小。 */
var AndroidSword.defaultPageSize: Int
    set(value) {
        touch()
        Paging.defaultPagingSize = value
    }
    get() {
        return Paging.defaultPagingSize
    }

/** 设置一个默认的布局 id，在使用 Fragments 中相关方法时，如果没有传入特定的容器 id  时，则使用设置的默认布局 id。【必须配置】  */
var AndroidSword.defaultFragmentContainerId: Int
    set(value) {
        touch()
        FragmentConfig.setDefaultContainerId(value)
    }
    get() {
        return FragmentConfig.defaultContainerId()
    }

/**设置默认的 Fragment 转场动画 */
var AndroidSword.defaultFragmentAnimator: FragmentAnimator
    set(value) {
        touch()
        FragmentConfig.setDefaultFragmentAnimator(value)
    }
    get() {
        return FragmentConfig.defaultFragmentAnimator()
    }

var AndroidSword.refreshViewFactory: RefreshViewFactory.Factory?
    set(value) {
        touch()
        RefreshViewFactory.registerFactory(value)
    }
    get() {
        return RefreshViewFactory.getFactory()
    }

/** 加载更多的方式，默认为滑动到底部时自动加载更多。*/
@LoadMode var AndroidSword.loadMoreMode: Int
    get() {
        return LoadMoreConfig.getLoadMode()
    }
    set(value) {
        LoadMoreConfig.setLoadMode(value)
    }

/** 用于创建 LoadingView。【必须配置】 */
var AndroidSword.loadingViewHostFactory: LoadingViewHostFactory?
    set(value) {
        touch()
        internalLoadingViewHostFactory = value
    }
    get() = internalLoadingViewHostFactory

var AndroidSword.refreshLoadViewFactory: RefreshLoadMoreViewFactory.Factory?
    set(value) {
        touch()
        RefreshLoadMoreViewFactory.registerFactory(value)
    }
    get() {
        return RefreshLoadMoreViewFactory.getFactory()
    }

/** 用于配置使用 epoxy 时，LoadMore Item 的视图 。 */
var AndroidSword.epoxyLoadMoreViewFactory: EpoxyLoadMoreViewFactory
    set(value) {
        touch()
        internalDefaultEpoxyLoadMoreViewFactory = value
    }
    get() = internalDefaultEpoxyLoadMoreViewFactory