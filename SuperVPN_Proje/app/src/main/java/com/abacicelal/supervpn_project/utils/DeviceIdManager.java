package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.provider.Settings;
import android.content.SharedPreferences;
import java.util.UUID;

public class DeviceIdManager {
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public static String getUniqueDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check for known bad IDs or null (9774d56d682e549c is a common generic ID on some emulators/devices)
        if (androidId == null || androidId.equals("9774d56d682e549c") || androidId.isEmpty()) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE);
            String uuid = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uuid);
                editor.apply();
            }
            return uuid;
        }
        return androidId;
    }
}
