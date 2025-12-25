package com.abacicelal.supervpn_project.utils;

import com.wireguard.android.backend.Tunnel;

public class WireGuardTunnel implements Tunnel {
    private final String name;

    public WireGuardTunnel(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onStateChange(State newState) {
        // Handle state changes if necessary
    }
}
