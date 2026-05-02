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

# 保留 Application 类
-keep class com.freewdkt.bck.MyApplication { *; }

# ========== 关键：保留所有数据类（Gson 需要） ==========
-keep class com.freewdkt.bck.data.** { *; }
-keepclassmembers class com.freewdkt.bck.data.** {
    # 保留所有构造函数（包括带参数的、默认的、Kotlin 生成的）
    public <init>(...);
    public <init>();
    # 保留伴生对象（如果有）
    *** Companion;
}

# 保留 Gson 需要的泛型签名和注解
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# 保留 Kotlin 元数据和内部类
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public *;
}

# 保留所有内部类的默认构造函数
-keepclassmembers class * {
    public <init>();
}

# ========== 保留所有枚举（Gson 可能用到） ==========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========== 第三方库规则 ==========
# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn org.conscrypt.**


# Markwon
-keep class io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**