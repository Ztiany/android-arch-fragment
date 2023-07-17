package com.android.base.fragment.anim

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.ScaleAnimation

interface FragmentAnimator {
    fun makeOpenEnter(): Animation?
    fun makeOpenExit(): Animation?
    fun makeCloseEnter(): Animation?
    fun makeCloseExit(): Animation?
}

