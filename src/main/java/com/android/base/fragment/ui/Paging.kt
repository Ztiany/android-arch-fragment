package com.android.base.fragment.ui


/**
 * @author Ztiany
 */
abstract class Paging @JvmOverloads constructor(
    val start: Int = defaultPagingStart,
    val size: Int = defaultPagingSize,
) {

    fun hasMore(size: Int): Boolean {
        return size >= this.size
    }

    abstract val total: Int

    abstract val current: Int

    abstract val next: Int

    companion object {
        var defaultPagingStart = 0
        var defaultPagingSize = 20
    }

}
