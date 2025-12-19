package com.celalabaci.dto.vpnserver;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VpnServerCreateUpdateDto {

    @NotEmpty(message = "Server name cannot be empty.")
    private String serverName;

    @NotEmpty(message = "Server IP address cannot be empty.")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Invalid IP address format.")
    private String serverIpAddress;

    @NotNull(message = "Country ID cannot be null.")
    private Long countryId;

    private boolean isActive = true;
}
