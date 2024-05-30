package com.android.base.fragment.list.paging3

import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter

fun PagingDataAdapter<*, *>.withDefaultLoadStateFooter(
    /** by default, load more footer is always shown. */
    displayLoadStateAsItem: ((loadState: LoadState) -> Boolean)? = null,
): ConcatAdapter {
    return withLoadStateFooter(DefaultLoadStateAdapter(this, displayLoadStateAsItem))
}