# Compose reflection targets
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# kotlinx-serialization (used for type-safe navigation routes)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.underthykilt.poketypes.**$$serializer { *; }
