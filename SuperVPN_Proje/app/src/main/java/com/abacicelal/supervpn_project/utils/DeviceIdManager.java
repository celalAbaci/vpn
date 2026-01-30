package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.util.UUID;

public class DeviceIdManager {
    private static final String PREF_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        // Try to get Android ID
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Known bad ID or null/empty
        if (deviceId == null || deviceId.isEmpty() || "9774d56d682e549c".equals(deviceId)) {
             SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
             deviceId = prefs.getString(KEY_DEVICE_ID, null);
             if (deviceId == null) {
                 deviceId = UUID.randomUUID().toString();
                 prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
             }
        }
        return deviceId;
    }

    public static void saveRegisteredDeviceId(Context context, Long id) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong("registered_device_id", id).apply();
    }

    public static Long getRegisteredDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long id = prefs.getLong("registered_device_id", -1);
        return id == -1 ? null : id;
    }
}
