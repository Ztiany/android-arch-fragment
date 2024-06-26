package com.android.base.fragment.list.paging3

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback

class IntIdentityDiffCallback<T : IntIdentity>(
    private val areTheItemsSame: (T, T) -> Boolean = { old, new -> old.id == new.id },
    private val areTheContentsSame: (T, T) -> Boolean = { old, new -> old == new },
    private val getChangedPayload: (T, T) -> Any? = { _: IntIdentity, _: IntIdentity -> null },
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

interface IntIdentity {
    val id: Int
}

class LongIdentityDiffCallback(
    private val oldList: List<LogIdentity>,
    private val newList: List<LogIdentity>,
    private val areItemSame: (LogIdentity, LogIdentity) -> Boolean = { old, new -> old.id == new.id },
    private val areContentSame: (LogIdentity, LogIdentity) -> Boolean = { old, new -> old == new },
    private val getChangePayload: (LogIdentity, LogIdentity) -> Any? = { _: LogIdentity, _: LogIdentity -> null },
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areContentSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
        getChangePayload(oldList[oldItemPosition], newList[newItemPosition])

}

interface LogIdentity {
    val id: Long
}

class StringIdentityDiffCallback(
    private val oldList: List<StringIdentity>,
    private val newList: List<StringIdentity>,
    private val areItemSame: (StringIdentity, StringIdentity) -> Boolean = { old, new -> old.id == new.id },
    private val areContentSame: (StringIdentity, StringIdentity) -> Boolean = { old, new -> old == new },
    private val getChangePayload: (StringIdentity, StringIdentity) -> Any? = { _: StringIdentity, _: StringIdentity -> null },
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areContentSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
        getChangePayload(oldList[oldItemPosition], newList[newItemPosition])

}

interface StringIdentity {
    val id: Long
}


class SimpleDiffCallback(
    private val oldList: List<Any>,
    private val newList: List<Any>,
    private val areItemSame: (Any, Any) -> Boolean,
    private val areContentSame: (Any, Any) -> Boolean,
    private val getChangePayload: (Any, Any) -> Any? = { _: Any, _: Any -> null },
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areContentSame(oldList[oldItemPosition], newList[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
        getChangePayload(oldList[oldItemPosition], newList[newItemPosition])

}