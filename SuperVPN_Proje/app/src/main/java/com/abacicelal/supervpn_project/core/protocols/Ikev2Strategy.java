package com.abacicelal.supervpn_project.core.protocols;

import android.content.Context;
import android.util.Log;

public class Ikev2Strategy implements IVpnStrategy {

    private static final String TAG = "Ikev2Strategy";
    private VpnStatusListener listener;
    private boolean isConnected = false;

    @Override
    public void connect(Context context, String configContent, String username, String password) {
        Log.d(TAG, "Connecting IKEv2...");
        if (listener != null) listener.onStatusChanged("CONNECTING", "Starting IKEv2...", "INFO");

        // Placeholder logic
        // In a real implementation, you would use VpnManager (API 30+) or StrongSwan lib

        // Simulating connection for now
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isConnected = true;
            if (listener != null) {
                listener.onConnected();
                listener.onStatusChanged("CONNECTED", "IKEv2 Connected", "INFO");
            }
        }, 2000);
    }

    @Override
    public void disconnect(Context context) {
        Log.d(TAG, "Disconnecting IKEv2...");
        isConnected = false;
        if (listener != null) listener.onDisconnected();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void setListener(VpnStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public String getProtocolName() {
        return "IKEV2";
    }
}
