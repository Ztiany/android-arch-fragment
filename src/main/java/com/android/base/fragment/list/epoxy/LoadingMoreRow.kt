package com.android.base.fragment.list.epoxy

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(autoLayout = ModelView.Size.MANUAL)
class LoadingMoreRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val epoxyLoadMoreView: EpoxyLoadMoreView = internalDefaultEpoxyLoadMoreViewFactory.inflateLoadingMoreView(this, 0)

    @ModelProp
    fun setState(@LoadMoreViewState state: Int) {
        when (state) {
            LoadMoreViewState.LOADING -> {
                epoxyLoadMoreView.onLoading()
            }

            LoadMoreViewState.COMPLETED_WITH_MORE -> {
                epoxyLoadMoreView.onCompleted(true)
            }

            LoadMoreViewState.COMPLETED_WITH_NO_MORE -> {
                epoxyLoadMoreView.onCompleted(false)
            }

            LoadMoreViewState.FAILED -> {
                epoxyLoadMoreView.onFailed()
            }

            LoadMoreViewState.CLICK_TO_LOAD -> {
                epoxyLoadMoreView.showClickToLoadMore()
            }
        }
    }

    @ModelProp
    fun setAutoHideWhenNoMore(autoHideWhenNoMore: Boolean) {
        epoxyLoadMoreView.autoHideWhenNoMore = autoHideWhenNoMore
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        this.setOnClickListener(listener)
    }

}