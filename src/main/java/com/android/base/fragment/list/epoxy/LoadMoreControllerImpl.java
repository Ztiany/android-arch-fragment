package com.android.base.fragment.list.epoxy;

import com.ztiany.loadmore.adapter.Direction;
import com.ztiany.loadmore.adapter.LoadMode;
import com.ztiany.loadmore.adapter.LoadMoreConfig;
import com.ztiany.loadmore.adapter.LoadMoreController;
import com.ztiany.loadmore.adapter.LoadMoreViewFactory;
import com.ztiany.loadmore.adapter.OnLoadMoreListener;
import com.ztiany.loadmore.adapter.OnRecyclerViewScrollBottomListener;

abstract class LoadMoreControllerImpl implements LoadMoreController {

    private boolean mHasMore = false;

    private boolean mStopAutoLoadWhenFailed = LoadMoreConfig.isStopAutoLoadWhenFailed();

    private OnLoadMoreListener mOnLoadMoreListener;

    private final static int STATUS_LOADING = 1;
    private final static int STATUS_FAIL = 2;
    private final static int STATUS_COMPLETE = 3;
    private final static int STATUS_PRE = 4;
    private int mCurrentStatus = STATUS_PRE;

    private long mPreviousTimeCallingLoadMore;
    private long mMixLoadMoreInterval = LoadMoreConfig.getMinLoadMoreInterval();

    private final boolean loadMoreCallMinimalTimeInterval;
    private final OnRecyclerViewScrollBottomListener mOnRecyclerViewScrollBottomListener;
    private boolean mAutoHideWhenNoMore = false;

    @LoadMode
    private int mLoadMode = LoadMoreConfig.getLoadMode();

    @Direction
    private int mDirection = Direction.UP;

    public LoadMoreControllerImpl(boolean useScrollListener, OnRecyclerViewScrollBottomListener onRecyclerViewScrollBottomListener) {
        loadMoreCallMinimalTimeInterval = useScrollListener;
        mOnRecyclerViewScrollBottomListener = onRecyclerViewScrollBottomListener;
        init();
    }

    void tryCallLoadMore(int direction) {
        if (mOnLoadMoreListener == null || !mOnLoadMoreListener.canLoadMore()) {
            return;
        }
        if (mCurrentStatus == STATUS_LOADING) {
            return;
        }
        if (isAutoLoad()) {
            if (mStopAutoLoadWhenFailed && mCurrentStatus == STATUS_FAIL) {
                return;
            }
            mCurrentStatus = STATUS_PRE;
            if (checkIfNeedCallLoadMoreWhenAutoMode(direction)) {
                callLoadMore();
            }
        } else {
            if (mCurrentStatus == STATUS_FAIL) {
                return;
            }
            if (mCurrentStatus == STATUS_COMPLETE && !mHasMore) {
                return;
            }
            mCurrentStatus = STATUS_PRE;
            showClickLoadMoreState();
        }
    }

    private boolean checkIfNeedCallLoadMoreWhenAutoMode(int direction) {
        if (direction != 0 && direction != mDirection) {
            return false;
        }
        if (loadMoreCallMinimalTimeInterval) {
            return System.currentTimeMillis() - mPreviousTimeCallingLoadMore >= mMixLoadMoreInterval;
        } else {
            return true;
        }
    }

    public void onClickLoadMoreView() {
        if (mLoadMode == LoadMode.AUTO_LOAD) {
            if ((mCurrentStatus == STATUS_FAIL)) {
                callLoadMore();
            }
        } else if (mLoadMode == LoadMode.CLICK_LOAD) {
            if (mCurrentStatus == STATUS_PRE || mCurrentStatus == STATUS_FAIL) {
                callLoadMore();
            }
        }
    }

    @Override
    public void setMinLoadMoreInterval(long mixLoadMoreInterval) {
        mMixLoadMoreInterval = mixLoadMoreInterval;
    }

    @Override
    public void stopAutoLoadWhenFailed(boolean stopAutoLoadWhenFailed) {
        mStopAutoLoadWhenFailed = stopAutoLoadWhenFailed;
    }

    @Override
    public void setLoadMoreDirection(@Direction int direction) {
        mDirection = direction;
    }

    @Override
    public void setLoadingTriggerThreshold(int loadingTriggerThreshold) {
        if (mOnRecyclerViewScrollBottomListener != null) {
            mOnRecyclerViewScrollBottomListener.setLoadingTriggerThreshold(loadingTriggerThreshold);
        }
    }

    @Override
    public void loadFailed() {
        mCurrentStatus = STATUS_FAIL;
        showLoadMoreFailedState();
    }

    @Override
    public void loadCompleted(final boolean hasMore) {
        loadCompleted(hasMore, false);
    }

    @Override
    public void loadCompleted(final boolean hasMore, final boolean appended) {
        mHasMore = hasMore;
        mCurrentStatus = STATUS_COMPLETE;
        // If the data is appended, the load more view will not be shown.
        // So we don't need to refresh the load more view.
        if (!appended) {
            showLoadMoreCompletedState(mHasMore);
        }
    }

    @Override
    public boolean isLoadingMore() {
        return mCurrentStatus == STATUS_LOADING;
    }

    @Override
    public void setLoadMode(@LoadMode int loadMode) {
        mLoadMode = loadMode;
    }

    @Override
    public void setLoadMoreViewFactory(LoadMoreViewFactory factory) {
        throw new UnsupportedOperationException("");
    }

    private boolean isAutoLoad() {
        return mLoadMode == LoadMode.AUTO_LOAD;
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    private void callLoadMore() {
        if (mCurrentStatus != STATUS_LOADING && mOnLoadMoreListener != null && mHasMore) {
            showLoadingMoreState();
            mCurrentStatus = STATUS_LOADING;
            mOnLoadMoreListener.onLoadMore();
            mPreviousTimeCallingLoadMore = System.currentTimeMillis();
        }
    }

    @Override
    public void setAutoHideWhenNoMore(boolean autoHiddenWhenNoMore) {
        if (mAutoHideWhenNoMore == autoHiddenWhenNoMore) {
            return;
        }
        mAutoHideWhenNoMore = autoHiddenWhenNoMore;
        refreshLoadMoreView();
    }

    private void init() {
        if (mLoadMode == LoadMode.CLICK_LOAD) {
            initClickLoadMoreViewStatus();
        } else {
            initAutoLoadMoreViewStatus();
        }
    }

    private void initAutoLoadMoreViewStatus() {
        switch (mCurrentStatus) {
            case STATUS_PRE:
            case STATUS_LOADING: {
                showLoadingMoreState();
                break;
            }
            case STATUS_FAIL: {
                showLoadMoreFailedState();
                break;
            }
            case STATUS_COMPLETE: {
                showLoadMoreCompletedState(mHasMore);
                break;
            }
        }
    }

    private void initClickLoadMoreViewStatus() {
        switch (mCurrentStatus) {
            case STATUS_PRE: {
                showClickLoadMoreState();
                break;
            }
            case STATUS_LOADING: {
                showLoadingMoreState();
                break;
            }
            case STATUS_FAIL: {
                showLoadMoreFailedState();
                break;
            }
            case STATUS_COMPLETE: {
                showLoadMoreCompletedState(mHasMore);
                break;
            }
        }
    }

    @Override
    public void setLoadingMore() {
        mCurrentStatus = STATUS_LOADING;
        showLoadingMoreState();
    }

    public boolean isAutoHideWhenNoMore() {
        return mAutoHideWhenNoMore;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Abstract methods
    ///////////////////////////////////////////////////////////////////////////

    abstract void showClickLoadMoreState();

    abstract void showLoadMoreCompletedState(boolean hasMore);

    abstract void showLoadMoreFailedState();

    abstract void showLoadingMoreState();

    protected abstract void refreshLoadMoreView();

}
