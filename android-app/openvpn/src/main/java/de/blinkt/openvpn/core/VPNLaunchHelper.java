package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import de.blinkt.openvpn.VpnProfile;

/**
 * Helper to launch VPN.
 * This mocks the actual OpenVPN launch process since the native library and full source
 * are part of the external 'ics-openvpn' project which we are integrating.
 *
 * In a real scenario with the library, this class wraps the AIDL or direct Intent calls.
 */
public class VPNLaunchHelper {

    private static final String TAG = "VPNLaunchHelper";

    public static void startOpenVpn(VpnProfile vp, Context context, String reason, boolean force) {
        Log.i(TAG, "Starting OpenVPN: " + vp.mName);

        // In the real integration, we would serialize the profile and send it to the OpenVPNService.
        // For this task, we will simulate the successful start of the service Intent.

        Intent intent = new Intent(context, OpenVPNService.class);
        intent.putExtra("de.blinkt.openvpn.profileUUID", vp.getUUIDString());
        intent.putExtra("de.blinkt.openvpn.profileVersion", vp.mVersion);
        intent.putExtra("de.blinkt.openvpn.REASON", reason);
        intent.putExtra("de.blinkt.openvpn.profileName", vp.mName);

        // Pass the config content directly if needed by the service logic we implemented
        // (Though typically ics-openvpn uses UUID lookup from ProfileManager)

        // Start the service
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
