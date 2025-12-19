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

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenVpnProvisioningStrategy implements VpnProvisioningStrategy {

    private final ISshAgentService sshAgentService;

    @Override
    public VpnProtocol getProtocol() {
        return VpnProtocol.OPENVPN;
    }

    @Override
    public AgentDTOs.OpenVpnCredentials provision(VpnServer server, User user, UserDevice device) {
        String clientName = user.getUsername() + "_" + device.getId();

        // Command: MENU_OPTION="1" CLIENT="testuser" PASS="1" ./openvpn-install.sh
        String createCommand = String.format(
                "MENU_OPTION=\"1\" CLIENT=\"%s\" PASS=\"1\" ./openvpn-install.sh",
                clientName
        );
        String catCommand = String.format("cat /root/%s.ovpn", clientName);
        String cleanCommand = String.format("rm /root/%s.ovpn", clientName);

        try {
            sshAgentService.connect(server.getServerIpAddress(), server.getSshUsername(), server.getSshPassword());

            log.info("Provisioning OpenVPN user: {}", clientName);
            sshAgentService.runCommand(createCommand);

            log.info("Reading OpenVPN config file...");
            String configContent = sshAgentService.runCommand(catCommand);

            if (configContent == null || configContent.isEmpty()) {
                throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "OpenVPN config file is empty.");
            }

            // Cleanup
            sshAgentService.runCommand(cleanCommand);

            return new AgentDTOs.OpenVpnCredentials(
                    "", // CA embedded
                    configContent, // FULL CONFIG
                    "", // Key embedded
                    "", // TLS embedded
                    "tcp",
                    443
            );
        } catch (Exception e) {
            log.error("OpenVPN Error: {}", e.getMessage());
            throw new ConfigGenerationException(MessageType.AGENT_PROVISIONING_FAILED, "OpenVPN provisioning error: " + e.getMessage());
        } finally {
            sshAgentService.disconnect();
        }
    }
}
