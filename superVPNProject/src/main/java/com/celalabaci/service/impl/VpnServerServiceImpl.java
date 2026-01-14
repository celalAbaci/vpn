package com.celalabaci.service.impl;

import com.celalabaci.dto.vpnserver.VpnServerDto;
import com.celalabaci.dto.vpnserver.VpnServerCreateUpdateDto;
import com.celalabaci.entity.Country;
import com.celalabaci.entity.VpnServer;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.VpnServerMapper;
import com.celalabaci.repository.CountryRepository;
import com.celalabaci.repository.VpnServerRepository;
import com.celalabaci.service.IVpnServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VpnServerServiceImpl implements IVpnServerService {

    @Autowired
    private VpnServerRepository vpnServerRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private VpnServerMapper vpnServerMapper;

    @Override
    public List<VpnServerDto> getActiveServersForUsers() {
        // --- DÜZELTME BURASI ---
        // Hata veren 'findByIsActiveTrue()' yerine 'findByActiveTrue()' kullanıldı.
        return vpnServerRepository.findByActiveTrue().stream()
                .map(vpnServerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VpnServerDto> getAllServersForAdmin() {
        return vpnServerRepository.findAll().stream()
                .map(vpnServerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public VpnServerDto getServerById(Long id) {
        VpnServer server = findServerById(id);
        return vpnServerMapper.toDto(server);
    }

    @Override
    @Transactional
    public VpnServerDto createServer(VpnServerCreateUpdateDto dto) {
        if (vpnServerRepository.existsByServerIpAddressIgnoreCase(dto.getServerIpAddress())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "A server with IP " + dto.getServerIpAddress() + " already exists.");
        }

        Country country = findCountryById(dto.getCountryId());
        VpnServer vpnServer = vpnServerMapper.toEntity(dto);
        vpnServer.setCountry(country);

        VpnServer savedServer = vpnServerRepository.save(vpnServer);
        return vpnServerMapper.toDto(savedServer);
    }

    @Override
    @Transactional
    public VpnServerDto updateServer(Long id, VpnServerCreateUpdateDto dto) {
        VpnServer existingServer = findServerById(id);

        if (!existingServer.getServerIpAddress().equalsIgnoreCase(dto.getServerIpAddress()) &&
                vpnServerRepository.existsByServerIpAddressIgnoreCase(dto.getServerIpAddress())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "A server with IP " + dto.getServerIpAddress() + " already exists.");
        }

        if (!existingServer.getCountry().getId().equals(dto.getCountryId())) {
            Country newCountry = findCountryById(dto.getCountryId());
            existingServer.setCountry(newCountry);
        }

        vpnServerMapper.updateEntityFromDto(dto, existingServer);
        VpnServer updatedServer = vpnServerRepository.save(existingServer);
        return vpnServerMapper.toDto(updatedServer);
    }

    @Override
    public void deleteServer(Long id) {
        if (!vpnServerRepository.existsById(id)) {
            throw new BaseException(MessageType.NO_RECORD_EXIST, "VPN Server with id " + id + " not found.");
        }
        vpnServerRepository.deleteById(id);
    }

    private VpnServer findServerById(Long id) {
        return vpnServerRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "VPN Server with id " + id + " not found."));
    }

    private Country findCountryById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Country with id " + id + " not found for the server."));
    }
}