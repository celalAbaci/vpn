package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * AES256-GCM ile şifreli SharedPreferences wrapper'ı.
 * JWT tokenlar ve cihaz kimliği gibi hassas veriler burada saklanır.
 *
 * Kullanım: Mevcut SharedPreferences çağrıları yerine bu sınıfın
 * statik metodlarını kullanın.
 */
public class SecurePrefsManager {

    private static final String TAG = "SecurePrefsManager";
    private static final String SECURE_FILE = "VPN_PREFS_SECURE";
    private static final String LEGACY_FILE = "VPN_PREFS";

    private static volatile SharedPreferences instance = null;

    private static SharedPreferences getPrefs(Context context) {
        if (instance == null) {
            synchronized (SecurePrefsManager.class) {
                if (instance == null) {
                    instance = createPrefs(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private static SharedPreferences createPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    SECURE_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, "EncryptedSharedPreferences baslatılamadı, fallback kullanılıyor", e);
            return context.getSharedPreferences(SECURE_FILE, Context.MODE_PRIVATE);
        }
    }

    /**
     * Uygulama başlangıcında bir kez çağrılmalı (SplashActivity).
     * Eski plain-text VPN_PREFS içindeki token/device_id'yi şifreli depoya taşır.
     */
    public static void initialize(Context context) {
        SharedPreferences legacy = context.getSharedPreferences(LEGACY_FILE, Context.MODE_PRIVATE);

        String authToken    = legacy.getString("AUTH_TOKEN", null);
        String refreshToken = legacy.getString("REFRESH_TOKEN", null);
        String deviceUuid   = legacy.getString("device_id", null);
        long   registeredId = legacy.getLong("registered_device_id", -1L);

        boolean hasData = authToken != null || refreshToken != null
                || deviceUuid != null || registeredId != -1L;
        if (!hasData) return;

        SharedPreferences.Editor editor = getPrefs(context).edit();
        if (authToken != null)    editor.putString("AUTH_TOKEN", authToken);
        if (refreshToken != null) editor.putString("REFRESH_TOKEN", refreshToken);
        if (deviceUuid != null)   editor.putString("device_id", deviceUuid);
        if (registeredId != -1L)  editor.putLong("registered_device_id", registeredId);
        editor.apply();

        // Eski plain-text kayıtları temizle
        legacy.edit()
                .remove("AUTH_TOKEN")
                .remove("REFRESH_TOKEN")
                .remove("device_id")
                .remove("registered_device_id")
                .apply();
    }

    // ── String ────────────────────────────────────────────────────────────────

    public static String getString(Context context, String key, String defValue) {
        try {
            return getPrefs(context).getString(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static void putString(Context context, String key, String value) {
        try {
            getPrefs(context).edit().putString(key, value).apply();
        } catch (Exception e) {
            Log.w(TAG, "putString basarısız: " + key);
        }
    }

    // ── Long ──────────────────────────────────────────────────────────────────

    public static long getLong(Context context, String key, long defValue) {
        try {
            return getPrefs(context).getLong(key, defValue);
        } catch (Exception e) {
            return defValue;
        }
    }

    public static void putLong(Context context, String key, long value) {
        try {
            getPrefs(context).edit().putLong(key, value).apply();
        } catch (Exception e) {
            Log.w(TAG, "putLong basarısız: " + key);
        }
    }

    // ── Remove ────────────────────────────────────────────────────────────────

    public static void remove(Context context, String key) {
        try {
            getPrefs(context).edit().remove(key).apply();
        } catch (Exception e) {
            Log.w(TAG, "remove basarısız: " + key);
        }
    }
}
