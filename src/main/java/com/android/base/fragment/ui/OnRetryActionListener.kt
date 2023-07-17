package com.android.base.fragment.ui

import com.android.base.fragment.ui.StateLayoutConfig.RetryableState

interface OnRetryActionListener {

    fun onRetry(@RetryableState state: Int)

}