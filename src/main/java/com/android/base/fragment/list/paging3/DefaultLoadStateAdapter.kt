package com.android.base.fragment.list.paging3

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter

internal class DefaultLoadStateAdapter(
    private val pagingAdapter: PagingDataAdapter<*, *>,
    private val displayLoadStateAsItem: ((loadState: LoadState) -> Boolean)? = null,
) : LoadStateAdapter<PagingLoadMoreViewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): PagingLoadMoreViewViewHolder {
        return internalDefaultPagingLoadMoreViewFactory.createPagingLoadMoreViewViewHolder(parent, loadState)
    }

    override fun onBindViewHolder(holder: PagingLoadMoreViewViewHolder, loadState: LoadState) {
        holder.handleLoadState(loadState, pagingAdapter)
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        if (displayLoadStateAsItem != null) {
            return super.displayLoadStateAsItem(loadState) || displayLoadStateAsItem(loadState)
        }
        return super.displayLoadStateAsItem(loadState) || internalDefaultPagingLoadMoreViewFactory.displayLoadStateAsItem(loadState)
    }

}