package com.android.base.fragment.anim

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.android.base.fragment.R

class DefaultNoAnimator(private val context: Context) : FragmentAnimator {

    override fun makeOpenEnter(): Animation? {
        return AnimationUtils.loadAnimation(context, R.anim.base_no_anim)
    }

    override fun makeOpenExit(): Animation? {
        return AnimationUtils.loadAnimation(context, R.anim.base_no_anim)
    }

    override fun makeCloseEnter(): Animation? {
        return AnimationUtils.loadAnimation(context, R.anim.base_no_anim)
    }

    override fun makeCloseExit(): Animation? {
        return AnimationUtils.loadAnimation(context, R.anim.base_no_anim)
    }

}
