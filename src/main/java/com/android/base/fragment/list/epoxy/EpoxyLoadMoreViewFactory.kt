package com.android.base.fragment.list.epoxy

import androidx.constraintlayout.widget.ConstraintLayout

interface EpoxyLoadMoreViewFactory {

    fun inflateLoadingMoreView(container: ConstraintLayout, direction: Int): EpoxyLoadMoreView

}
