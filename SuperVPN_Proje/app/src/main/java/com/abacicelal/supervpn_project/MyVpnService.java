package com.abacicelal.supervpn_project;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import java.io.IOException;

/**
 * Basic VpnService implementation.
 * Currently serves as a placeholder for future Java-based VPN protocols (like IKEv2 or custom Super protocol)
 * if they are implemented directly in Java without native libraries.
 * OpenVPN uses its own service (de.blinkt.openvpn.core.OpenVPNService) from the embedded library.
 */
public class MyVpnService extends VpnService {

    private ParcelFileDescriptor vpnInterface = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // This service can be started by strategies that require a Java VpnService.
        // For now, it's just a skeleton.
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
