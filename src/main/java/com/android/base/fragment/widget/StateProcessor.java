package com.android.base.fragment.widget;

import android.content.res.TypedArray;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.base.fragment.ui.StateLayoutConfig;

/**
 * @author Ztiany
 */
public abstract class StateProcessor {

    protected abstract void onInitialize(SimpleMultiStateLayout simpleMultiStateLayout);

    protected abstract void onParseAttrs(TypedArray typedArray);

    protected abstract void processStateInflated(@StateLayoutConfig.ViewState int viewState, @NonNull View view);

    protected abstract StateLayoutConfig getStateLayoutConfigImpl();

}
