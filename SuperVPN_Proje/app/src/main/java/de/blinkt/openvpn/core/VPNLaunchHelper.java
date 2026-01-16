package de.blinkt.openvpn.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import de.blinkt.openvpn.VpnProfile;

public class VPNLaunchHelper {

    public static void startOpenVpn(VpnProfile startProfile, Context context) {
        // Fix: Use the correct method signature that might be expected by the underlying service
        // or ensure the profile is fully populated.

        // The prompt mentioned "Bağlantı kopukluğu var... ConfigParser yapısını onar... Handshake hatasız hale getir".
        // Often, the issue is that the inline data (CA, Cert, Key) isn't correctly passed to the native process
        // or the connection status isn't monitored correctly.

        // We ensure the intent is constructed properly.
        Intent intent = new Intent(context, OpenVPNService.class);
        intent.putExtra(OpenVPNService.EXTRA_PROFILE_UUID, startProfile.getUUID().toString());
        intent.putExtra(OpenVPNService.EXTRA_START_REASON, "User requested connection");
        intent.putExtra(OpenVPNService.EXTRA_START_CONNECTION, true);

        // Ensure profile is written to disk or accessible if the service reads from file
        ProfileManager.getInstance(context).saveProfile(context, startProfile);

        // Start Service
        context.startService(intent);
    }
}
