package com.abacicelal.supervpn_project.utils;

import android.content.Context;
import java.util.UUID;

public class DeviceIdManager {
    private static final String KEY_DEVICE_ID = "device_id";

    public static String getDeviceId(Context context) {
        String deviceId = SecurePrefsManager.getString(context, KEY_DEVICE_ID, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            SecurePrefsManager.putString(context, KEY_DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    public static void saveRegisteredDeviceId(Context context, Long id) {
        SecurePrefsManager.putLong(context, "registered_device_id", id);
    }

    public static Long getRegisteredDeviceId(Context context) {
        long id = SecurePrefsManager.getLong(context, "registered_device_id", -1L);
        return id == -1L ? null : id;
    }
}
