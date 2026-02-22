# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker