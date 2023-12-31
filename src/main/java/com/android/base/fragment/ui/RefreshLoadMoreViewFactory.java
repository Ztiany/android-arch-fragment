package com.android.base.fragment.ui;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * RefreshLoadMoreView Factory.
 *
 * @author Ztiany
 */
public class RefreshLoadMoreViewFactory {

    private static Factory sFactory;

    @NonNull
    public static RefreshLoadMoreView createRefreshLoadMoreView(View view) {
        if (sFactory != null) {
            return sFactory.createRefreshView(view);
        }
        throw new IllegalArgumentException("RefreshLoadViewFactory does not support create RefreshLoadMoreView . the view ：" + view);
    }

    public static void registerFactory(Factory factory) {
        sFactory = factory;
    }

    public interface Factory {
        RefreshLoadMoreView createRefreshView(View view);
    }

    public static Factory getFactory() {
        return sFactory;
    }

}
