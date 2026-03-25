package de.blinkt.openvpn.core;

import android.os.Build;
import java.security.InvalidKeyException;

/**
 * ics-openvpn JNI köprüsü.
 *
 * readelf analizi:
 *   libovpnutil.so exports: getJNIAPI, getOpenVPN2GitVersion, getOpenVPN3GitVersion
 *   libovpnexec.so:         RegisterNatives ile dinamik (doğrudan Java çağrısı desteklenmiyor)
 *
 * Kütüphane yükleme sırası:
 *   1. libovpnutil.so → getJNIAPI, getOpenVPN2/3GitVersion
 *   2. libopenvpn.so  → Ana OpenVPN kütüphanesi
 *   3. libovpnexec.so → exec wrapper (RegisterNatives ile başka sınıflara register eder)
 */
public class NativeUtils {

    private static boolean sLibLoaded = false;
    private static String sLoadError = null;

    static {
        sLibLoaded = tryLoad("ovpnutil");
        tryLoad("openvpn");
        tryLoad("ovpnexec");
    }

    private static boolean tryLoad(String libName) {
        try {
            System.loadLibrary(libName);
            android.util.Log.i("NativeUtils", "Loaded lib" + libName + ".so OK");
            return true;
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("NativeUtils", "FAILED lib" + libName + ".so: " + e.getMessage());
            if (sLoadError == null) sLoadError = e.getMessage();
            return false;
        }
    }

    public static boolean isNativeLoaded() {
        return sLibLoaded;
    }

    public static String getLoadError() {
        return sLoadError;
    }

    // ============================================================
    // Gerçek JNI export'ları — libovpnutil.so'dan
    // ============================================================

    /**
     * JNI API string'ini döndürür (library versiyonu/yetenekleri)
     * Export: Java_de_blinkt_openvpn_core_NativeUtils_getJNIAPI
     */
    public static native String getJNIAPI();

    /**
     * OpenVPN2 git commit versiyonunu döndürür
     * Export: Java_de_blinkt_openvpn_core_NativeUtils_getOpenVPN2GitVersion
     */
    public static native String getOpenVPN2GitVersion();

    /**
     * OpenVPN3 git commit versiyonunu döndürür
     * Export: Java_de_blinkt_openvpn_core_NativeUtils_getOpenVPN3GitVersion
     */
    public static native String getOpenVPN3GitVersion();

    // ============================================================
    // libovpnexec.so RegisterNatives ile dinamik register ediyor
    // Bu fonksiyonlar başka bir sınıfta (muhtemelen OpenVPNService)
    // register edilmiş olabilir — şimdilik stub
    // ============================================================

    public static native void jniclose(int fd);

    public static native byte[] rsasign(byte[] data, int pkeyRef) throws InvalidKeyException;

    // Helper
    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }
}