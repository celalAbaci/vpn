package com.celalabaci.service;

import com.celalabaci.dto.vpnserver.VpnServerDto;
import com.celalabaci.dto.vpnserver.VpnServerCreateUpdateDto;

import java.util.List;

public interface IVpnServerService {
    List<VpnServerDto> getActiveServersForUsers();
    List<VpnServerDto> getAllServersForAdmin();
    VpnServerDto getServerById(Long id);
    VpnServerDto createServer(VpnServerCreateUpdateDto dto);
    VpnServerDto updateServer(Long id, VpnServerCreateUpdateDto dto);
    void deleteServer(Long id);
}
