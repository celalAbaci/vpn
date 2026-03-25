package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.util.Base64;
import android.util.Log;

import com.scottyab.rootbeer.RootBeer;

import java.security.MessageDigest;

/**
 * Uygulama güvenlik kontrolleri: tamper tespiti, root tespiti, anti-debug.
 *
 * KURULUM ADIMI — APK imza hash'ini doldurmak için:
 *   1. gradlew assembleRelease ile APK'yı imzalayın
 *   2. İlk çalıştırmada EXPECTED_SIGNATURE boş iken logcat'te
 *      "İmza hash'i: XXXX" satırını görün
 *   3. O hash'i EXPECTED_SIGNATURE sabitine yazın
 *   4. Yeniden build edin
 */
public class SecurityUtils {

    private static final String TAG = "SecurityUtils";

    /**
     * Beklenen APK imza SHA-256 hash'i (Base64, no-wrap).
     * İlk release build sonrası bu değer doldurulmalı.
     * Boş bırakılırsa imza kontrolü atlanır.
     */
    private static final String EXPECTED_SIGNATURE = "mPmjZVkJ8959W5DBJXwUluNygInGQM3XMlHedsgzwHI=";

    // ── Tamper Detection ──────────────────────────────────────────────────────

    /**
     * APK imzasının beklenen değerle eşleşip eşleşmediğini kontrol eder.
     * EXPECTED_SIGNATURE boşsa her zaman true döner (geliştirme modu).
     */
    @SuppressWarnings("deprecation")
    public static boolean isValidSignature(Context context) {
        if (EXPECTED_SIGNATURE == null || EXPECTED_SIGNATURE.isEmpty()) {
            return true;
        }
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature sig : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(sig.toByteArray());
                String current = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                if (EXPECTED_SIGNATURE.equals(current)) return true;
                Log.w(TAG, "Mevcut imza hash'i: " + current);
            }
        } catch (Exception e) {
            Log.e(TAG, "İmza kontrolü hatası", e);
        }
        return false;
    }

    // ── Root Detection ────────────────────────────────────────────────────────

    /**
     * Cihazın root'lu olup olmadığını kontrol eder.
     * isRootedWithoutBusyBoxCheck() kullanılır — false positive oranı daha düşük.
     */
    public static boolean isRooted(Context context) {
        try {
            RootBeer rootBeer = new RootBeer(context);
            return rootBeer.isRootedWithoutBusyBoxCheck();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Anti-Debugging ────────────────────────────────────────────────────────

    /**
     * Bir debugger'ın bağlı olup olmadığını kontrol eder.
     * Release buildde aktif — BuildConfig.ENABLE_SECURITY_CHECKS ile kontrol edilmeli.
     */
    public static boolean isDebuggerAttached() {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger();
    }

    // ── Emulator Detection ────────────────────────────────────────────────────

    /**
     * Uygulamanın emülatör üzerinde çalışıp çalışmadığını kontrol eder.
     */
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
