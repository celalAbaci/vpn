package de.blinkt.openvpn.core;

import android.content.Context;
import de.blinkt.openvpn.VpnProfile;

public class ProfileManager {

    private static VpnProfile mLastConnectedProfile;
    private static VpnProfile mTemporaryProfile;

    public static VpnProfile get(Context context, String profileUUID, int version, int tries) {
        // Basitlik için geçici profili döndürüyoruz
        if (mTemporaryProfile != null && mTemporaryProfile.getUUIDString().equals(profileUUID)) {
            return mTemporaryProfile;
        }
        return mLastConnectedProfile;
    }

    public static VpnProfile getLastConnectedProfile(Context context) {
        return mLastConnectedProfile;
    }

    public static void setConnectedVpnProfile(Context context, VpnProfile profile) {
        mLastConnectedProfile = profile;
    }

    public static void setConntectedVpnProfileDisconnected(Context context) {
        // Bağlantı koptuğunda yapılacaklar (Boş bırakabiliriz)
    }

    public static void setTemporaryProfile(Context context, VpnProfile profile) {
        mTemporaryProfile = profile;
    }

    public static VpnProfile getAlwaysOnVPN(Context context) {
        return mLastConnectedProfile;
    }
}