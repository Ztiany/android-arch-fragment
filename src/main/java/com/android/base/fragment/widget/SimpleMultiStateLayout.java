package com.android.base.fragment.widget;

import static com.android.base.fragment.ui.StateLayoutConfig.CONTENT;
import static com.android.base.fragment.ui.StateLayoutConfig.EMPTY;
import static com.android.base.fragment.ui.StateLayoutConfig.ERROR;
import static com.android.base.fragment.ui.StateLayoutConfig.LOADING;
import static com.android.base.fragment.ui.StateLayoutConfig.ViewState;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.android.base.fragment.R;
import com.android.base.fragment.ui.StateLayout;
import com.android.base.fragment.ui.StateLayoutConfig;

import timber.log.Timber;

/**
 * @author Ztiany
 */
public class SimpleMultiStateLayout extends MultiStateLayout implements StateLayout {

    private StateProcessor mStateProcessor;

    private StateListener mStateListener;

    public SimpleMultiStateLayout(Context context) {
        this(context, null);
    }

    public SimpleMultiStateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleMultiStateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setListener();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleMultiStateLayout, defStyle, defStyle);
        initProcessor(typedArray);

        mStateProcessor.onInitialize(this);
        mStateProcessor.onParseAttrs(typedArray);

        typedArray.recycle();
    }

    private void initProcessor(TypedArray typedArray) {
        String processorPath = typedArray.getString(R.styleable.SimpleMultiStateLayout_msl_state_processor);

        if (!TextUtils.isEmpty(processorPath)) {
            try {
                Class<?> processorClass = Class.forName(processorPath);
                mStateProcessor = (StateProcessor) processorClass.newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Timber.e("initProcessor() called can not instance processor: %s", processorPath);
            }
        }

        if (mStateProcessor == null) {
            mStateProcessor = new StateActionProcessor();
        }
    }

    private void setListener() {
        super.setStateListener(new StateListener() {
            @Override
            public void onStateChanged(@StateLayoutConfig.ViewState int viewState) {
                if (mStateListener != null) {
                    mStateListener.onStateChanged(viewState);
                }
            }

            @Override
            public void onStateInflated(@StateLayoutConfig.ViewState int viewState, @NonNull android.view.View view) {
                if (mStateListener != null) {
                    mStateListener.onStateInflated(viewState, view);
                }
                processStateInflated(viewState, view);
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
