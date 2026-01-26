package com.abacicelal.supervpn_project.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;

import java.io.StringReader;

public class VpnConnector {

    private static final String TAG = "VpnConnector";

    public void connectToOpenVpn(Context context, String configContent, String username, String password) {
        try {
            ConfigParser cp = new ConfigParser();
            cp.parseConfig(new StringReader(configContent));
            VpnProfile profile = cp.convertProfile();

            profile.mName = "SuperVPN Connection";
            if (username != null) profile.mUsername = username;
            if (password != null) profile.mPassword = password;
            profile.mInlineConfig = configContent;

            // Set as temporary profile so ProfileManager can serve it to the Service
            ProfileManager.setTemporaryProfile(context, profile);

            // Launch VPN Service via Intent
            // Using the action defined in ics-openvpn
            Intent intent = new Intent("de.blinkt.openvpn.LaunchVPN");
            intent.setPackage(context.getPackageName());
            intent.putExtra("de.blinkt.openvpn.profileUUID", profile.getUUIDString());
            intent.putExtra("de.blinkt.openvpn.shortcutProfileUUID", profile.getUUIDString());
            intent.putExtra("de.blinkt.openvpn.autopull", true); // Auto-connect
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
            Log.d(TAG, "VPN Launch Intent sent for UUID: " + profile.getUUIDString());

        } catch (Exception e) {
            Log.e(TAG, "Error connecting to VPN", e);
        }
    }

    public void disconnect(Context context) {
        Intent intent = new Intent("de.blinkt.openvpn.disconnect");
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }
}
