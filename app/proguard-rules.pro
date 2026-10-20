# ProGuard rules for ShoreLine
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
