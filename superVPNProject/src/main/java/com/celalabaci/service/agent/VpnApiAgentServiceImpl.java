package com.celalabaci.service.agent;

import com.celalabaci.dto.agent.AgentDTOs;
import com.celalabaci.dto.config.VpnProtocol;
import com.celalabaci.entity.User;
import com.celalabaci.entity.UserDevice;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.ConfigGenerationException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.service.agent.strategy.VpnProvisioningStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VpnApiAgentServiceImpl implements VpnApiAgentService {

    private final Map<VpnProtocol, VpnProvisioningStrategy> strategies;

    // Constructor injection for the list of strategies, converted to a Map
    public VpnApiAgentServiceImpl(List<VpnProvisioningStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(VpnProvisioningStrategy::getProtocol, Function.identity()));
    }

    @Override
    public AgentDTOs.OpenVpnCredentials provisionOpenVpnUser(VpnServer server, User user, UserDevice device) {
        return (AgentDTOs.OpenVpnCredentials) getStrategy(VpnProtocol.OPENVPN).provision(server, user, device);
    }

    @Override
    public AgentDTOs.IkeV2Credentials provisionIkeV2User(VpnServer server, User user, UserDevice device) {
        return (AgentDTOs.IkeV2Credentials) getStrategy(VpnProtocol.IKEV2).provision(server, user, device);
    }

    @Override
    public AgentDTOs.V2RayCredentials provisionV2RayUser(VpnServer server, User user, UserDevice device) {
        return (AgentDTOs.V2RayCredentials) getStrategy(VpnProtocol.V2RAY).provision(server, user, device);
    }

    @Override
    public AgentDTOs.SuperCredentials provisionSuperUser(VpnServer server, User user, UserDevice device) {
        return (AgentDTOs.SuperCredentials) getStrategy(VpnProtocol.SUPER).provision(server, user, device);
    }

    @Override
    public boolean ping() {
        return true;
    }

    private VpnProvisioningStrategy getStrategy(VpnProtocol protocol) {
        VpnProvisioningStrategy strategy = strategies.get(protocol);
        if (strategy == null) {
            throw new ConfigGenerationException(MessageType.GENERAL_EXCEPTION, "No strategy found for protocol: " + protocol);
        }
        return strategy;
    }
}
