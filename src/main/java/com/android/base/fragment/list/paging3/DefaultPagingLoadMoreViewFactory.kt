package com.android.base.fragment.list.paging3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import com.android.base.fragment.R

internal var internalDefaultPagingLoadMoreViewFactory: PagingLoadMoreViewFactory = DefaultPagingLoadMoreViewFactory()

private class DefaultPagingLoadMoreViewViewHolder(item: View) : PagingLoadMoreViewViewHolder(item) {

    private val msgTv = item.findViewById<TextView>(R.id.base_id_loading_more_item_tv)
    private val progressBar = item.findViewById<ProgressBar>(R.id.base_id_loading_more_item_pb)
    private val context: Context
        get() = itemView.context

    private val noMoreMsg = context.getString(com.ztiany.loadmore.adapter.R.string.adapter_no_more_message)
    private val failMsg = context.getString(com.ztiany.loadmore.adapter.R.string.adapter_load_more_fail)
    private val loadCompleted = context.getString(com.ztiany.loadmore.adapter.R.string.adapter_load_completed)

    override fun handleLoadState(loadState: LoadState, pagingAdapter: PagingDataAdapter<*, *>) {
        when (loadState) {
            is LoadState.Error -> {
                msgTv.visibility = View.VISIBLE
                msgTv.text = failMsg
                progressBar.visibility = View.INVISIBLE
                itemView.setOnClickListener {
                    pagingAdapter.retry()
                }
            }

            LoadState.Loading -> {
                itemView.setOnClickListener(null)
                msgTv.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }

            is LoadState.NotLoading -> {
                itemView.setOnClickListener(null)
                progressBar.visibility = View.INVISIBLE
                msgTv.visibility = View.VISIBLE
                if (loadState.endOfPaginationReached) {
                    msgTv.text = noMoreMsg
                } else {
                    msgTv.text = loadCompleted
                }
            }
        }

    }

}

class DefaultPagingLoadMoreViewFactory : PagingLoadMoreViewFactory {

    override fun createPagingLoadMoreViewViewHolder(parent: ViewGroup, loadState: LoadState): PagingLoadMoreViewViewHolder {
        return DefaultPagingLoadMoreViewViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.base_layout_paging_load_more, parent, false)
        )
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return true
    }

}