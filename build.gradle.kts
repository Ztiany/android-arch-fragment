plugins {
    alias(libs.plugins.app.common.library)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    namespace = "com.android.base.fragment"

    //如果不想生成某个布局的绑定类，可以在根视图添加 tools:viewBindingIgnore="true" 属性。
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    //  基础库
    api(project(":base:core"))
    api(project(":base:activity"))
    api(project(":base:adapter"))
    api(project(":base:viewbinding"))
    api(project(":base:mvi"))
    api(libs.ztiany.archdelegate)
    api(libs.ztiany.uistate)

    // androidx
    api(libs.androidx.annotations)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.recyclerview)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.swiperefreshlayout)
    api(libs.google.ui.material)
    api(libs.androidx.viewpager2)

    // kotlin
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.coroutines.android)

    // log
    api(libs.jakewharton.timber)

    // epoxy
    api(libs.airbnb.epoxy)
    kapt(libs.airbnb.epoxy.apt)
}