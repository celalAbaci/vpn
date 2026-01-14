package de.blinkt.openvpn.core;

import android.os.Build;
import java.security.InvalidKeyException;

public class NativeUtils {

    // Kütüphaneyi statik olarak yüklüyoruz
    static {
        try {
            // 'libovpnexec.so' dosyasını yükler
            System.loadLibrary("ovpnexec");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    // JNI (Java Native Interface) Metotları
    // Bu metotlar C++ tarafındaki kodları çağırır.

    public static native String getNativeAPI();

    public static native String getJNIAPI();

    public static native void jniclose(int fd);

    public static native byte[] rsasign(byte[] data, int pkeyRef) throws InvalidKeyException;

    // Helper metotlar (BuildConfig kullanmadan)
    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }
}