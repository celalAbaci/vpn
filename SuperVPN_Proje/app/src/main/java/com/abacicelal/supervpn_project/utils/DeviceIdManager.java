package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.UUID;

public class DeviceIdManager {

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    private static final String PREFS_NAME = "VPN_PREFS";

    public static synchronized String getDeviceId(Context context) {
        // 1. Try to get from Shared Prefs
        String uniqueID = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(PREF_UNIQUE_ID, null);

        if (uniqueID == null) {
            // 2. Try ANDROID_ID
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            // 3. Validate ANDROID_ID (sometimes it's null or known bugged value "9774d56d682e549c")
            if (!TextUtils.isEmpty(androidId) && !"9774d56d682e549c".equals(androidId)) {
                uniqueID = androidId;
            } else {
                // 4. Fallback to UUID
                uniqueID = UUID.randomUUID().toString();
            }

            // 5. Save to Prefs
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREF_UNIQUE_ID, uniqueID)
                    .apply();
        }

        return uniqueID;
    }
}
