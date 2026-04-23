# 基本优化选项
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# 优化选项
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 移除 Log 代码（Release 包中所有 Log 调用会被移除）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# 保留你的 Application 类（已添加）
-keep class com.freewdkt.bck.MyApplication { *; }

# ========== 关键：保留所有数据类（Gson 需要） ==========
-keep class com.freewdkt.bck.data.** { *; }
-keepclassmembers class com.freewdkt.bck.data.** { <init>(); }

# 保留 Gson 需要的泛型签名和注解
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 保留 Kotlin 元数据（data class 等）
-keep class kotlin.Metadata { *; }

# 保留内部类的默认构造函数（Gson 需要）
-keepclassmembers class * {
    public <init>();
}

# 如果你使用了 OkHttp，建议添加以下规则（通常依赖自带，但安全起见）
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn org.conscrypt.**

# 如果你使用了 Coil，添加以下规则避免警告
-keep class coil.** { *; }
-dontwarn coil.**

# 如果你使用了 Markwon，添加以下规则
-keep class io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**