# Fragment Extensions

Check out [android-architecture-practice](https://github.com/Ztiany/android-architecture-practice) for details.

## Usage of BaseStateFragment

your layout should be like the following:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.android.base.fragment.widget.SimpleMultiStateView
    xmlns:android="http://schemas.android.com/apk/res/android"
    style="@style/BaseArchStyle.SimpleMultiStateView">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.android.base.fragment.widget.SimpleMultiStateView>
```

or like this if you need refreshing function:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.android.base.fragment.widget.SimpleMultiStateView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    style="@style/BaseArchStyle.SimpleMultiStateView">

    <com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout
        android:id="@+id/base_refresh_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:srl_target_id="@id/recycler_view">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

    </com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout>
    
</com.android.base.fragment.widget.SimpleMultiStateView>
```

when you set `FragmentModuleConfig.retryByAutoRefresh` as your layout should be like the following:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/base_refresh_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:srl_target_id="@id/recycler_view">

    <com.android.base.fragment.widget.SimpleMultiStateLayout style="@style/Widget.BaseArch.SimpleMultiStateLayout">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@id/recycler_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

    </com.android.base.fragment.widget.SimpleMultiStateLayout>

</com.android.base.fragment.widget.ScrollChildSwipeRefreshLayout>
```
