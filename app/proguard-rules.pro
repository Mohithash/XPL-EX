-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# XPL-EX core (Xposed reflection targets)
-keep class eu.faircode.xlua.XLua {*; }
-keep class eu.faircode.xlua.XParam {*; }
-keepnames class eu.faircode.xlua.** {*; }

# LuaJ VM
-dontwarn org.luaj.vm2.**
-keepnames class org.luaj.vm2.** {*; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep enum com.bumptech.glide.** {*; }

# Xposed API
-keep class de.robv.android.xposed.** {*; }
-dontwarn de.robv.android.xposed.**

# LSPosed Hidden API Bypass
-keep class org.lsposed.hiddenapibypass.** {*; }

# libsu
-keep class com.topjohnwu.superuser.** {*; }
