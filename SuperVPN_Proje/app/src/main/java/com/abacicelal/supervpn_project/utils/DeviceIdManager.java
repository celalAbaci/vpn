package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.provider.Settings;
import java.util.UUID;
import android.content.SharedPreferences;

public class DeviceIdManager {

    private static final String PREF_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        // 1. Try to get saved ID from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedId = prefs.getString(KEY_DEVICE_ID, null);

        if (savedId != null) {
            return savedId;
        }

        // 2. Try to get ANDROID_ID
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // 3. Validate ANDROID_ID
        if (androidId == null || androidId.isEmpty() || androidId.equals("9774d56d682e549c") || androidId.length() < 6) {
             // 4. Fallback to UUID if invalid
             androidId = UUID.randomUUID().toString();
        }

        // 5. Save and return
        prefs.edit().putString(KEY_DEVICE_ID, androidId).apply();
        return androidId;
    }
}
