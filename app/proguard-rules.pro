# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
# Keep all data classes used in API responses
-keep class com.example.emergencycommunicationsystem.data.models.** { *; }
# Keep any other models if necessary

# Room
-keep class * extends androidx.room.RoomDatabase

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# OSMDroid
-keep class org.osmdroid.** { *; }

# ML Kit (if needed, usually they have consumer proguard rules)
-keep class com.google.mlkit.** { *; }
