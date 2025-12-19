package com.celalabaci.service.agent.strategy;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.service.agent.ISshAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IkeV2ProvisioningStrategy implements VpnProvisioningStrategy {

    private final ISshAgentService sshAgentService;

    @Override
    public VpnProtocol getProtocol() {
        return VpnProtocol.IKEV2;
    }

    @Override
    public AgentDTOs.IkeV2Credentials provision(VpnServer server, User user, UserDevice device) {
        String username = user.getUsername() + device.getId();
        String password = UUID.randomUUID().toString().substring(0, 10);

        String addCommand = String.format(
                "docker exec -i dataguard-ikev2 sh -c \"echo '%s : EAP \\\"%s\\\"' >> /etc/ipsec.secrets\"",
                username, password
        );
        String reloadCommand = "docker exec -i dataguard-ikev2 ipsec secrets";

        try {
            sshAgentService.connect(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword());

            log.info("Adding IKEv2 user: {}", username);
            sshAgentService.runCommand(addCommand);

            log.info("Reloading IKEv2 secrets...");
            sshAgentService.runCommand(reloadCommand);

            return new AgentDTOs.IkeV2Credentials(
                    username,
                    password,
                    server.getServerIpAddress(),
                    server.getServerIpAddress()
            );
        } catch (Exception e) {
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "IKEv2 provisioning error: " + e.getMessage());
        } finally {
            sshAgentService.disconnect();
        }
    }
}
