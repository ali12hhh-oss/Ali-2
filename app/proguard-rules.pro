# Prodline AI R8 rules.

# Keep line numbers so re-mapped release stack traces stay debuggable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson reflects over the project model graph (EditRecipe / EditOperation
# subclasses). Field names must survive obfuscation or saved projects and
# drafts silently deserialize empty.
-keep class com.tharunbirla.librecuts.models.** { *; }
-keep class com.tharunbirla.librecuts.utils.ProjectSerializer$* { *; }

# ffmpeg-kit loads JNI natives and dispatches through callback interfaces.
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.smartexceptionjava.** { *; }
-dontwarn com.arthenica.**

# ExoPlayer extension renderers are resolved reflectively at runtime.
-keep class com.google.android.exoplayer2.ext.** { *; }
