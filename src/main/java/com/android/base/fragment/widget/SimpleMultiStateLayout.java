package com.android.base.fragment.widget;

import static com.android.base.fragment.ui.StateLayoutConfig.CONTENT;
import static com.android.base.fragment.ui.StateLayoutConfig.EMPTY;
import static com.android.base.fragment.ui.StateLayoutConfig.ERROR;
import static com.android.base.fragment.ui.StateLayoutConfig.LOADING;
import static com.android.base.fragment.ui.StateLayoutConfig.ViewState;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.base.fragment.R;
import com.android.base.fragment.ui.StateLayout;
import com.android.base.fragment.ui.StateLayoutConfig;

/**
 * @author Ztiany
 */
public class SimpleMultiStateLayout extends MultiStateLayout implements StateLayout {

    private final StateProcessor mStateProcessor;

    private StateListener mStateListener;

    public SimpleMultiStateLayout(Context context) {
        this(context, null);
    }

    public SimpleMultiStateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMultiStateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setStateListener();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleMultiStateLayout, defStyle, defStyle);
        mStateProcessor = new StateActionProcessor();
        mStateProcessor.onInitialize(this);
        mStateProcessor.onParseAttrs(typedArray);
        typedArray.recycle();
    }

    private void setStateListener() {
        super.setStateListener(new StateListener() {
            @Override
            public void onStateChanged(@StateLayoutConfig.ViewState int viewState) {
                if (mStateListener != null) {
                    mStateListener.onStateChanged(viewState);
                }
            }

            @Override
            public void onStateInflated(@StateLayoutConfig.ViewState int viewState, @NonNull android.view.View view) {
                processStateInflated(viewState, view);
                if (mStateListener != null) {
                    mStateListener.onStateInflated(viewState, view);
                }
            }
        });
    }

    private void processStateInflated(@ViewState int viewState, @NonNull View view) {
        mStateProcessor.processStateInflated(viewState, view);
    }

    @Override
    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    @Override
    public void showContentLayout() {
        setLayoutState(CONTENT);
    }

    @Override
    public void showLoadingLayout() {
        setLayoutState(LOADING);
    }

    @Override
    public void showEmptyLayout() {
        setLayoutState(EMPTY);
    }

    @Override
    public void showErrorLayout() {
        setLayoutState(ERROR);
    }

    @Override
    public void showRequesting() {
        setLayoutState(StateLayoutConfig.REQUESTING);
    }

    @Override
    public void showBlank() {
        setLayoutState(StateLayoutConfig.BLANK);
    }

    @Override
    public void showNetErrorLayout() {
        setLayoutState(StateLayoutConfig.NET_ERROR);
    }

    @Override
    public void showServerErrorLayout() {
        setLayoutState(StateLayoutConfig.SERVER_ERROR);
    }

    @Override
    public StateLayoutConfig getStateLayoutConfig() {
        return mStateProcessor.getStateLayoutConfigImpl();
    }

    @Override
    @ViewState
    public int currentStatus() {
        return getViewState();
    }

}
