package de.blinkt.openvpn.core;

import android.content.Context;
import android.os.Build;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Vector;

public class NetworkUtils {

    public static Vector<String> getLocalNetworks(Context c, boolean ipv6) {
        Vector<String> networks = new Vector<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isLoopback() || !networkInterface.isUp())
                    continue;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress ip = interfaceAddress.getAddress();
                    if (ip instanceof Inet4Address && !ipv6) {
                        String netmask;
                        // Android'in eski sürümleri için prefix length hesabı
                        short prefix = interfaceAddress.getNetworkPrefixLength();

                        // Basit CIDR formatı: 192.168.1.5/24
                        networks.add(ip.getHostAddress() + "/" + prefix);

                    } else if (ip instanceof Inet6Address && ipv6) {
                        // IPv6 desteği
                        short prefix = interfaceAddress.getNetworkPrefixLength();
                        networks.add(ip.getHostAddress() + "/" + prefix);
                    }
                }
            }
        } catch (SocketException e) {
            VpnStatus.logError("Error getting local networks: " + e.getLocalizedMessage());
        }
        return networks;
    }
}