package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import android.provider.Settings;
import java.util.UUID;

public class DeviceIdManager {
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    public static String getDeviceId(Context context) {
        String uniqueID = context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE)
                                 .getString(PREF_UNIQUE_ID, null);

        if (uniqueID == null) {
            uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            if (uniqueID == null || uniqueID.isEmpty() || "9774d56d682e549c".equals(uniqueID)) {
                uniqueID = UUID.randomUUID().toString();
            }

            context.getSharedPreferences("VPN_PREFS", Context.MODE_PRIVATE)
                   .edit()
                   .putString(PREF_UNIQUE_ID, uniqueID)
                   .apply();
        }

        return uniqueID;
    }
}
