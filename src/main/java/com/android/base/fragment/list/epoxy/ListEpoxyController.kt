package com.android.base.fragment.list.epoxy

import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.TypedEpoxyController
import com.android.base.core.AndroidSword
import com.android.base.fragment.ui.ListDataHost
import com.ztiany.loadmore.adapter.LoadMoreController
import timber.log.Timber

abstract class ListEpoxyController<T> : TypedEpoxyController<List<T>>(), ListDataHost<T> {

    private val loadMoreHelper by lazy {
        EpoxyControllerLoadMoreHelper {
            requestModelBuildInternally()
        }
    }

    final override fun buildModels(data: List<T>) {
        buildListModels(data)
        if (data.isNotEmpty()) {
            with(loadMoreHelper) {
                buildLoadMoreModels(data.size)
            }
        }
    }

    abstract fun buildListModels(data: List<T>)

    override fun submitData(data: List<T>) {
        setData(data)
    }

    override fun isEmpty(): Boolean {
        return currentData.isNullOrEmpty()
    }

    override fun getListSize(): Int {
        return currentData?.size ?: 0
    }

    private fun requestModelBuildInternally() {
        setData(currentData ?: emptyList())
    }

    fun setUpLoadMore(recyclerView: RecyclerView, triggerLoadMoreByScroll: Boolean = AndroidSword.loadMoreTriggerByScroll): LoadMoreController {
        return loadMoreHelper.setUpLoadMore(recyclerView, triggerLoadMoreByScroll)
    }

}