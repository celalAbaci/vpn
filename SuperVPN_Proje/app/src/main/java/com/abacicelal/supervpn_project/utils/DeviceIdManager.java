package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.UUID;

public class DeviceIdManager {

    private static final String PREF_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            deviceId = generateDeviceId(context);
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }

    private static String generateDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Known bad ID
        if ("9774d56d682e549c".equals(androidId) || TextUtils.isEmpty(androidId)) {
            return UUID.randomUUID().toString();
        }
        return androidId; // Or combine with UUID
    }

    // For compatibility with existing code that might use Long IDs (if backend expects Long for registered devices)
    // But Guest Login uses String unique ID.
    public static Long getRegisteredDeviceId(Context context) {
        // Implementation depends on where Registered ID is stored.
        // For now returning null to force lookup or registration.
        return null;
    }
}
