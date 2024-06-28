package com.android.base.fragment.tool

class HandlingProcedure internal constructor(
    private val defaultHandling: (() -> Unit)? = null,
) {

    fun continueDefaultProcedure() {
        defaultHandling?.invoke()
    }

}