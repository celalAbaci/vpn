package com.abacicelal.supervpn_project.core.protocols;

import android.content.Context;

public interface IVpnStrategy {
    /**
     * Connect to the VPN using the provided configuration.
     * @param context Context for service binding
     * @param configContent The configuration content (e.g., .ovpn file content)
     * @param username Username (if required)
     * @param password Password (if required)
     */
    void connect(Context context, String configContent, String username, String password);

    /**
     * Disconnect the VPN.
     * @param context Context
     */
    void disconnect(Context context);

    /**
     * Check if currently connected.
     * @return true if connected
     */
    boolean isConnected();

    /**
     * Set a listener for status updates.
     * @param listener The listener
     */
    void setListener(VpnStatusListener listener);

    /**
     * Get the protocol name.
     */
    String getProtocolName();
}
