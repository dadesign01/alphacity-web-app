# ========================================
# 공통 속성
# ========================================
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, Exceptions, SourceFile, LineNumberTable

# ========================================
# Application / Hilt
# ========================================
-keep class com.alphacity.stamptour.StampTourApplication { *; }
-keep class * extends androidx.startup.Initializer
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *
-keep class hilt_aggregated_deps.** { *; }
-keep class **_HiltModules* { *; }
-keep class **_HiltComponents* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# ========================================
# Retrofit / OkHttp
# ========================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okio.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========================================
# kotlinx.serialization
# ========================================
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.alphacity.stamptour.**$$serializer { *; }
-keepclassmembers class com.alphacity.stamptour.** {
    *** Companion;
}
-keepclasseswithmembers class com.alphacity.stamptour.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# 모든 DTO/모델 클래스 보존
-keep class com.alphacity.stamptour.network.dto.** { *; }
-keep class com.alphacity.stamptour.model.** { *; }

# ========================================
# Kakao SDK (Login + Map)
# ========================================
-keep class com.kakao.sdk.** { *; }
-keep interface com.kakao.sdk.** { *; }
-keep class com.kakao.vectormap.** { *; }
-keep interface com.kakao.vectormap.** { *; }
-keep class com.kakao.maps.** { *; }
-keep class com.kakao.** { *; }
-dontwarn com.kakao.**

# ========================================
# Naver Login SDK
# ========================================
-keep class com.navercorp.nid.** { *; }
-keep interface com.navercorp.nid.** { *; }
-dontwarn com.navercorp.nid.**

# ========================================
# Firebase
# ========================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ========================================
# Google Maps
# ========================================
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.maps.android.**

# ========================================
# Room
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ========================================
# Kotlin / Coroutines
# ========================================
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.coroutines.SafeContinuation { volatile <fields>; }
-dontwarn kotlinx.coroutines.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# Enum
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ========================================
# Compose Lifecycle / ViewModel
# ========================================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# ========================================
# R8: Kotlin metadata 경고 무시
# ========================================
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn javax.lang.model.element.Modifier
