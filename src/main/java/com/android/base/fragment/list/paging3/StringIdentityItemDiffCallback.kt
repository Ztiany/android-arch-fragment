package com.android.base.fragment.list.paging3

import androidx.recyclerview.widget.DiffUtil.ItemCallback

class StringIdentityItemDiffCallback<T : StringIdentity>(
    private val areTheItemsSame: (T, T) -> Boolean = { old, new -> old.id == new.id },
    private val areTheContentsSame: (T, T) -> Boolean = { old, new -> old == new },
    private val getChangedPayload: (T, T) -> Any? = { _: StringIdentity, _: StringIdentity -> null },
) : ItemCallback<T>() {

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

interface StringIdentity {
    val id: String
}