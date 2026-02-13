# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Gson
# Keep all data classes used in API responses and requests
# Use -keep,allowobfuscation if you want to obfuscate but keep the fields
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keep class com.example.emergencycommunicationsystem.data.** { *; }
-keep class com.example.emergencycommunicationsystem.data.models.** { *; }
-keep class com.example.emergencycommunicationsystem.network.** { *; }

# Prevent R8 from removing generic signatures which causes "Class cannot be cast to ParameterizedType"
-keepattributes Signature

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# OSMDroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**
-keep class com.github.nkzawa.** { *; }
-dontwarn com.github.nkzawa.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
-keep class com.example.emergencycommunicationsystem.MyApplication { *; }
