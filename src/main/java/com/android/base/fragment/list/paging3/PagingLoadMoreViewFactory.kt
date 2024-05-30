package com.android.base.fragment.list.paging3

import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class PagingLoadMoreViewViewHolder(item: View) : ViewHolder(item) {

    abstract fun handleLoadState(loadState: LoadState, pagingAdapter: PagingDataAdapter<*, *>)

}

interface PagingLoadMoreViewFactory {

    fun createPagingLoadMoreViewViewHolder(parent: ViewGroup, loadState: LoadState): PagingLoadMoreViewViewHolder

    fun displayLoadStateAsItem(loadState: LoadState): Boolean

}