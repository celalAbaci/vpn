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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# OkHttp — isteğe bağlı SSL provider'lar Android'de bulunmaz, uyarıları gizle
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Retrofit & Gson — serileştirme için tut
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.abacicelal.supervpn_project.remote.model.** { *; }
-keep class com.google.gson.** { *; }

# libv2ray / Xray — JNI arayüzlerini koru
-keep class libv2ray.** { *; }

# de.blinkt.openvpn — OpenVPN kütüphanesi
-keep class de.blinkt.openvpn.** { *; }

# Release buildde debug/verbose/info logları tamamen kaldır
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Stack trace'lerde kaynak dosya isimlerini gizle
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Güvenlik utility sınıfları
-keep class com.abacicelal.supervpn_project.utils.SecurePrefsManager { *; }
-keep class com.abacicelal.supervpn_project.utils.SecurityUtils { *; }

# RootBeer kütüphanesi
-keep class com.scottyab.rootbeer.** { *; }
-dontwarn com.scottyab.rootbeer.**

# AndroidX Security (EncryptedSharedPreferences)
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**