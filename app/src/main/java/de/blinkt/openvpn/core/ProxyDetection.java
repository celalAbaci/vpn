package de.blinkt.openvpn.core;

import android.net.Proxy;
import android.net.ProxyInfo;
import android.os.Build;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import de.blinkt.openvpn.VpnProfile;

public class ProxyDetection {

    public static SocketAddress detectProxy(VpnProfile profile) {
        // Profilde özel bir proxy ayarı varsa onu kullan
        if (profile.mConnections != null && profile.mConnections.length > 0) {
            VpnProfile.Connection connection = profile.mConnections[0];
            if (connection.mProxyType == VpnProfile.Connection.ProxyType.HTTP ||
                    connection.mProxyType == VpnProfile.Connection.ProxyType.SOCKS5) {
                if (connection.mServerName != null) {
                    return new InetSocketAddress(connection.mServerName, Integer.parseInt(connection.mServerPort));
                }
            }
        }

        // Yoksa sistemin proxy ayarlarını algılamaya çalış
        try {
            String host = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");

            if (host != null && portStr != null) {
                int port = Integer.parseInt(portStr);
                return new InetSocketAddress(host, port);
            }
        } catch (Exception e) {
            VpnStatus.logError("Error detecting proxy: " + e.getLocalizedMessage());
        }

        return null;
    }
}