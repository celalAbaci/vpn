package com.abacicelal.supervpn_project.core.protocols;

import android.content.Context;
import android.util.Log;

public class SuperStrategy implements IVpnStrategy {

    private static final String TAG = "SuperStrategy";
    private VpnStatusListener listener;
    private boolean isConnected = false;

    @Override
    public void connect(Context context, String configContent, String username, String password) {
        Log.d(TAG, "Connecting Super Protocol...");
        if (listener != null) listener.onStatusChanged("CONNECTING", "Starting Super Protocol...", "INFO");

        // Placeholder logic for custom protocol

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isConnected = true;
            if (listener != null) {
                listener.onConnected();
                listener.onStatusChanged("CONNECTED", "Super Protocol Connected", "INFO");
            }
        }, 1500);
    }

    @Override
    public void disconnect(Context context) {
        Log.d(TAG, "Disconnecting Super Protocol...");
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
        return "SUPER";
    }
}
