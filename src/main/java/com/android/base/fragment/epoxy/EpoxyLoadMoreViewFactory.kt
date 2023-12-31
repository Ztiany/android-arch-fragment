package com.android.base.fragment.epoxy

import androidx.constraintlayout.widget.ConstraintLayout

interface EpoxyLoadMoreViewFactory {

    fun inflateLoadingMoreView(container: ConstraintLayout, direction: Int): EpoxyLoadMoreView

}
