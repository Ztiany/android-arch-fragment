package com.android.base.fragment.anim

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.ScaleAnimation

class DefaultScaleAnimator : FragmentAnimator {

    private val decelerateQuint: Interpolator = DecelerateInterpolator(2.5f)
    private val decelerateCubic: Interpolator = DecelerateInterpolator(1.5f)

    override fun makeOpenEnter(): Animation {
        return makeOpenCloseAnimation(1.125f, 1.0f, 0f, 1f)
    }

    override fun makeOpenExit(): Animation {
        return makeOpenCloseAnimation(1.0f, .975f, 1f, 0f)
    }

    override fun makeCloseEnter(): Animation {
        return makeOpenCloseAnimation(.975f, 1.0f, 0f, 1f)
    }

    override fun makeCloseExit(): Animation {
        return makeOpenCloseAnimation(1.0f, 1.075f, 1f, 0f)
    }

    private fun makeOpenCloseAnimation(startScale: Float, endScale: Float, startAlpha: Float, endAlpha: Float): Animation {
        val animDuration = 220
        val set = AnimationSet(false)
        val scale = ScaleAnimation(startScale, endScale, startScale, endScale, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f)
        scale.interpolator = decelerateQuint
        scale.duration = animDuration.toLong()
        set.addAnimation(scale)
        val alpha = AlphaAnimation(startAlpha, endAlpha)
        alpha.interpolator = decelerateCubic
        alpha.duration = animDuration.toLong()
        set.addAnimation(alpha)
        return set
    }

}
