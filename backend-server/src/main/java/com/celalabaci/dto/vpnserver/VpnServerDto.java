package com.celalabaci.dto.vpnserver;

import com.celalabaci.dto.country.CountryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VpnServerDto {
    private Long id;
    private String serverName;
    private String serverIpAddress;
    private CountryDto country; // İlişkili ülke bilgisini de DTO olarak gösteriyoruz.
    private Float currentLoadPercentage;
    private Integer currentConnectedUsers;
    private boolean isActive;
}
