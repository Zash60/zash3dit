# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line numbers, uncomment this to hide the original
# source file name.
#-renamesourcefileattribute SourceFile

# FFmpegKit rules
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.mobileffmpeg.** { *; }
-dontwarn org.bytedeco.javacpp.**
-dontwarn org.bytedeco.ffmpeg.**

# Room rules
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase {
    public static <fields>;
    public static *;
}
-keep class * extends androidx.room.Dao {
    public @androidx.room.Query <methods>;
    public @androidx.room.Insert <methods>;
    public @androidx.room.Update <methods>;
    public @androidx.room.Delete <methods>;
}
-dontwarn androidx.room.paging.**

# Compose rules
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ExoPlayer rules
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# Coil rules
-keep class coil.** { *; }
-dontwarn coil.**

# DataStore rules
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# General rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# AAPT rules
-keep class androidx.appcompat.widget.** { *; }
-keep class androidx.appcompat.app.** { *; }
-keep class androidx.appcompat.view.** { *; }

# Keep data class members
-keepclassmembers class * extends kotlin.Any {
    public final * $FF;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}