plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.freewdkt.bck"
    compileSdk = 35

    kotlin {
        jvmToolchain(11)
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.freewdkt.bck"
        minSdk = 26
        targetSdk = 35
        versionCode = 109
        versionName = "1.0.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // 必须同时开启代码混淆和资源压缩
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"   // 确保文件名正确
            )
        }
        // 为了 debug 方便，debug 模式可以不开混淆
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    // ========== 基础 AndroidX ==========
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ========== Material Design ==========
    implementation(libs.material)
    // 如果 libs.material 版本不够新，可用下面这行替换上面
    // implementation("com.google.android.material:material:1.13.0")

    // ========== 网络 & 图片 ==========
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")    // 图片加载（同时用于 Markwon）
    implementation("com.github.chrisbanes:PhotoView:2.3.0") // 图片缩放

    // ========== Markdown 渲染 ==========
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")        // 表格支持
    implementation("io.noties.markwon:image-glide:4.6.2")       // Glide 图片插件
    implementation("io.noties.markwon:recycler-table:4.6.2")    // 长表格 RecyclerView 支持

    // ========== UI 组件增强 ==========
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")  // 加密 SharedPreferences

    // ========== Jetpack Compose (如果未使用可全部删除) ==========
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // ========== 测试 ==========
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

configurations.all {
    exclude(group = "org.jetbrains", module = "annotations-java5")
}