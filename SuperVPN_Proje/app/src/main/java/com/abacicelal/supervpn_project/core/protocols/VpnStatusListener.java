package com.abacicelal.supervpn_project.core.protocols;

import android.content.Intent;

public interface VpnStatusListener {
    void onStatusChanged(String state, String message, String level);
    void onConnected();
    void onDisconnected();
    void onError(String error);
    /**
     * Called when VPN permission is required.
     * @param intent The intent to launch to request permission.
     */
    void onPermissionRequired(Intent intent);
}
