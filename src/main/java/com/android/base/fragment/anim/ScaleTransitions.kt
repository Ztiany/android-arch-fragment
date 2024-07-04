package com.android.base.fragment.anim

import android.content.Context
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.ScaleAnimation

class ScaleTransitions : FragmentTransitions {

    private val decelerateQuint: Interpolator = DecelerateInterpolator(2.5F)
    private val decelerateCubic: Interpolator = DecelerateInterpolator(1.5F)

    override fun makeOpenEnterAnimation(context: Context): Animation {
        return makeOpenCloseAnimation(1.125F, 1.0F, 0F, 1F)
    }

    override fun makeOpenExitAnimation(context: Context): Animation {
        return makeOpenCloseAnimation(1.0F, 1.0F, 1F, 0F)
    }

    override fun makeCloseEnterAnimation(context: Context): Animation {
        return makeOpenCloseAnimation(1.0F, 1.0F, 0F, 1F)
    }

    override fun makeCloseExitAnimation(context: Context): Animation {
        return makeOpenCloseAnimation(1.0F, 1.075f, 1F, 0F)
    }

    override fun makeOpenEnterAttributes(context: Context): TransitingAttribute? {
        return null
    }

    override fun makeOpenExitAttributes(context: Context): TransitingAttribute? {
        return null
    }

    override fun makeCloseEnterAttributes(context: Context): TransitingAttribute? {
        return null
    }

    override fun makeCloseExitAttributes(context: Context): TransitingAttribute? {
        return null
    }

    private fun makeOpenCloseAnimation(startScale: Float, endScale: Float, startAlpha: Float, endAlpha: Float): Animation {
        val set = AnimationSet(false)

        val scale = ScaleAnimation(
            startScale, endScale,
            startScale, endScale,
            Animation.RELATIVE_TO_SELF, .5F,
            Animation.RELATIVE_TO_SELF, .5F
        )

        scale.interpolator = decelerateQuint
        scale.duration = 350
        set.addAnimation(scale)

        val alpha = AlphaAnimation(startAlpha, endAlpha)
        alpha.interpolator = decelerateCubic
        alpha.duration = 350

        set.addAnimation(alpha)

        return set
    }

}