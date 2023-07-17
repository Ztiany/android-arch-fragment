package com.android.base.fragment.ui

import android.content.Context

typealias LoadingViewHostFactory = (context: Context) -> LoadingViewHost

internal var internalLoadingViewHostFactory: LoadingViewHostFactory? = null
