package com.celalabaci.service.agent.strategy;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SuperProvisioningStrategy implements VpnProvisioningStrategy {

    @Override
    public VpnProtocol getProtocol() {
        return VpnProtocol.SUPER;
    }

    @Override
    public AgentDTOs.SuperCredentials provision(VpnServer server, User user, UserDevice device) {
        // SUPER protocol specific implementation.
        // Keeping the logic of generating a specific link format.

        String secretKey = UUID.randomUUID().toString().replace("-", "");
        String superLink = String.format(
                "super://%s@%s:%d?name=%s",
                secretKey,
                server.getServerIpAddress(),
                443, // Default port
                user.getUsername()
        );

        log.info("SUPER protocol user created: {}", user.getUsername());

        return new AgentDTOs.SuperCredentials(
                superLink,
                secretKey,
                server.getServerIpAddress()
        );
    }
}
