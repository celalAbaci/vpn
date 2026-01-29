package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.UUID;

public class DeviceIdManager {

    private static final String PREF_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getUniqueDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String uuid = prefs.getString(KEY_DEVICE_ID, null);

        if (uuid == null) {
            // 1. Try Android ID
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            // Known bad ID or null
            if (TextUtils.isEmpty(androidId) || "9774d56d682e549c".equals(androidId)) {
                uuid = UUID.randomUUID().toString();
            } else {
                uuid = androidId;
            }

            // Save for future
            prefs.edit().putString(KEY_DEVICE_ID, uuid).apply();
        }

        return uuid;
    }
}
