package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.UUID;

public class DeviceIdManager {
    private static final String PREFS_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedId = prefs.getString(KEY_DEVICE_ID, null);

        if (!TextUtils.isEmpty(savedId)) {
            return savedId;
        }

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Known bad ID in some emulator/devices
        if (TextUtils.isEmpty(androidId) || "9774d56d682e549c".equals(androidId)) {
            androidId = UUID.randomUUID().toString();
        }

        prefs.edit().putString(KEY_DEVICE_ID, androidId).apply();
        return androidId;
    }
}
