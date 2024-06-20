package com.android.base.fragment.anim

import android.view.animation.Animation

interface FragmentAnimator {

    fun makeOpenEnter(): Animation?

    fun makeOpenExit(): Animation?

    fun makeCloseEnter(): Animation?

    fun makeCloseExit(): Animation?

}

