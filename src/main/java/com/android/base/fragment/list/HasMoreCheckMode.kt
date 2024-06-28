package com.android.base.fragment.list

import androidx.annotation.IntDef
import com.android.base.fragment.list.HasMoreCheckMode.Companion.BY_LOADED_SIZE
import com.android.base.fragment.list.HasMoreCheckMode.Companion.BY_PAGE_SIZE

@IntDef(value = [BY_PAGE_SIZE, BY_LOADED_SIZE])
annotation class HasMoreCheckMode {
    companion object {
        const val BY_PAGE_SIZE = 1
        const val BY_LOADED_SIZE = 2
    }
}