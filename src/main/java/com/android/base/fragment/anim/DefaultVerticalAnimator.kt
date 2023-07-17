package com.android.base.fragment.anim

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.android.base.fragment.R

class DefaultVerticalAnimator(private val context: Context) : FragmentAnimator {

    override fun makeOpenEnter(): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.base_v_fragment_open_enter)
    }

    override fun makeOpenExit(): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.base_v_fragment_open_exit)
    }

    override fun makeCloseEnter(): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.base_v_fragment_close_enter)
    }

    override fun makeCloseExit(): Animation {
        return AnimationUtils.loadAnimation(context, R.anim.base_v_fragment_close_exit)
    }

}