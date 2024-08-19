package com.android.base.fragment.list.segment

import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.android.base.fragment.ui.AutoPaging
import com.android.base.fragment.ui.SegmentedListDataHost
import com.android.base.fragment.ui.SegmentedListLayoutHost

/**
 * @see BaseListFragment
 */
abstract class CommonBaseListFragment<T, VB : ViewBinding> : BaseListFragment<T, Int, VB>() {

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
     *  2. You should call [setUpList] to return a real [SegmentedListLayoutHost].
     */
    abstract override fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, Int>

    protected fun setUpList(listDataHost: SegmentedListDataHost<T>): SegmentedListLayoutHost<T, Int> {
        return setUpList(listDataHost, AutoPaging())
    }

}

/**
 * @see BaseListFragment
 */
abstract class CommonBaseListDialogFragment<T, VB : ViewBinding> : BaseListDialogFragment<T, Int, VB>() {

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
     *  2. You should call [setUpList] to return a real [SegmentedListLayoutHost].
     */
    abstract override fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, Int>

    protected fun setUpList(listDataHost: SegmentedListDataHost<T>): SegmentedListLayoutHost<T, Int> {
        return setUpList(listDataHost, AutoPaging())
    }

}

/**
 * @see BaseList2Fragment
 */
abstract class CommonBaseList2Fragment<T, VB : ViewBinding> : BaseList2Fragment<T, Int, VB>() {

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
     *  2. You should call [setUpList] to return a real [SegmentedListLayoutHost].
     */
    abstract override fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, Int>

    protected fun setUpList(listDataHost: SegmentedListDataHost<T>): SegmentedListLayoutHost<T, Int> {
        return setUpList(listDataHost, AutoPaging())
    }

}

/**
 * @see BaseList2Fragment
 */
abstract class CommonBaseList2DialogFragment<T, VB : ViewBinding> : BaseList2DialogFragment<T, Int, VB>() {

    /**
     *  1. This method will be called before [onViewCreated] and [onSetupCreatedView].
     *  2. You should call [setUpList] to return a real [SegmentedListLayoutHost].
     */
    abstract override fun provideListImplementation(view: View, savedInstanceState: Bundle?): SegmentedListLayoutHost<T, Int>

    protected fun setUpList(listDataHost: SegmentedListDataHost<T>): SegmentedListLayoutHost<T, Int> {
        return setUpList(listDataHost, AutoPaging())
    }

}