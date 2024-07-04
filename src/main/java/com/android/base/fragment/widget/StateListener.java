package com.android.base.fragment.widget;

import android.view.View;

import androidx.annotation.NonNull;

import com.android.base.fragment.ui.StateLayoutConfig;

public interface StateListener {

    /**
     * Callback for when the {@link StateLayoutConfig.ViewState} has changed
     *
     * @param viewState The {@link StateLayoutConfig.ViewState} that was switched to
     */
    void onStateChanged(@StateLayoutConfig.ViewState int viewState);

    /**
     * Callback for when a {@link StateLayoutConfig.ViewState} has been inflated
     *
     * @param viewState The {@link StateLayoutConfig.ViewState} that was inflated
     * @param view      The {@link View} that was inflated
     */
    void onStateInflated(@StateLayoutConfig.ViewState int viewState, @NonNull View view);

}