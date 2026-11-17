-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep class com.google.firebase.** { *; }
-keep class com.connectx.social.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
