package com.android.base.fragment.tool

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment

class FragmentInfo(
    val clazz: Class<out Fragment>,
    val tag: String = clazz.name,
    val pageId: Int = -1,
    val title: String = "",
    /** If the fragment will be added to the back stack. */
    val stackName: String? = null,
    private val arguments: Bundle? = null,
) {

    private var createdFragment: Fragment? = null

    val instance: Fragment?
        get() = createdFragment

    fun setInstance(fragment: Fragment?) {
        createdFragment = fragment
    }

    fun newFragment(context: Context): Fragment {
        // It is not necessary to use FragmentFactory all the time.
        return Fragment.instantiate(context, clazz.getName(), arguments)
    }

}