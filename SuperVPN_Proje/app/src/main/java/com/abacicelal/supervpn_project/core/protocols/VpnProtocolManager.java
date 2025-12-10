package com.abacicelal.supervpn_project.core.protocols;

public class VpnProtocolManager {
    private static VpnProtocolManager instance;
    private IVpnStrategy currentStrategy;

    private VpnProtocolManager() {
        // Default strategy or null
    }

    public static synchronized VpnProtocolManager getInstance() {
        if (instance == null) {
            instance = new VpnProtocolManager();
        }
        return instance;
    }

    public void setStrategy(IVpnStrategy strategy) {
        this.currentStrategy = strategy;
    }

    public IVpnStrategy getStrategy() {
        return currentStrategy;
    }

    public boolean isConnected() {
        return currentStrategy != null && currentStrategy.isConnected();
    }
}
