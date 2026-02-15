# Retrofit & OkHttp
# Retrofit's Kotlin coroutine support relies on generic signature information (Continuation<? super T>).
# R8 can strip this unless we explicitly keep the attributes.
-keepattributes Signature, Exceptions, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes KotlinMetadata

# Keep Kotlin metadata class itself (safe, small, and avoids reflection edge cases).
-keep class kotlin.Metadata { *; }

# Ensure Continuation isn't removed; generic signature must remain for suspend service methods.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep Retrofit service methods and their annotations/signatures.
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Be explicit: keep your Retrofit service interfaces (prevents signature stripping on methods).
-keep interface com.example.emergencycommunicationsystem.network.** { *; }
-keep class com.example.emergencycommunicationsystem.network.** { *; }
-keep interface com.example.emergencycommunicationsystem.data.network.** { *; }
-keep class com.example.emergencycommunicationsystem.data.network.** { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Gson
# Keep all data classes used in API responses and requests
-keep class com.example.emergencycommunicationsystem.data.** { *; }
-keep class com.example.emergencycommunicationsystem.data.models.** { *; }

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


