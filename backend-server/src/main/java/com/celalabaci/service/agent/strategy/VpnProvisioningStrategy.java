package com.celalabaci.service.agent.strategy;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.Device;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;

/**
 * Strategy interface for VPN provisioning.
 * Each implementation handles a specific VPN protocol (OpenVPN, IKEv2, V2Ray, etc.).
 */
public interface VpnProvisioningStrategy {

    /**
     * Returns the protocol this strategy supports.
     */
    VpnProtocol getProtocol();

    /**
     * Provisions a user/device on the VPN server.
     * @param server The VPN server entity.
     * @param user The user requesting the config.
     * @param device The device info.
     * @return Credentials/Config DTO specific to the protocol (casted to Object or generic wrapper).
     */
    Object provision(VpnServer server, User user, UserDevice device);

    /**
     * Provisions a guest device on the VPN server.
     * @param server The VPN server entity.
     * @param device The guest device info.
     * @return Credentials/Config DTO specific to the protocol.
     */
    default Object provisionGuest(VpnServer server, Device device) {
        throw new UnsupportedOperationException("Guest provisioning not implemented for protocol: " + getProtocol());
    }
}
