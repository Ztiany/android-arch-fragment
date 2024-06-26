package com.android.base.fragment.list.paging3

import androidx.recyclerview.widget.DiffUtil

class LongIdentityItemDiffCallback<T : LongIdentity>(
    private val areTheItemsSame: (T, T) -> Boolean = { old, new ->
        old::class == new::class && old.id == new.id
    },
    private val areTheContentsSame: (T, T) -> Boolean = { old, new -> old == new },
    private val getChangedPayload: (T, T) -> Any? = { _: LongIdentity, _: LongIdentity -> null },
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return areTheItemsSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return areTheContentsSame(oldItem, newItem)
    }

    override fun getChangePayload(oldItem: T, newItem: T): Any? {
        return getChangedPayload(oldItem, newItem)
    }

}

interface LongIdentity {
    val id: Long
}
