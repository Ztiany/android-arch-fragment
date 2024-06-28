package com.android.base.fragment.tool

class HandingProcedure internal constructor(
    private val defaultHandling: (() -> Unit)? = null,
) {

    fun continueDefaultProcedure() {
        defaultHandling?.invoke()
    }

}