package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.util.UUID;

public class DeviceIdManager {
    private static final String PREFS_NAME = "VPN_PREFS";
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(KEY_DEVICE_ID, null);

        if (deviceId == null) {
            // Try Android ID
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            // "9774d56d682e549c" is a known bad ID on some versions
            if (androidId != null && !androidId.equals("9774d56d682e549c")) {
                deviceId = androidId;
            } else {
                // Fallback to UUID
                deviceId = UUID.randomUUID().toString();
            }

            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        }
        return deviceId;
    }
}
