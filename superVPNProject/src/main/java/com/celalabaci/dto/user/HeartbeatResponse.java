package com.celalabaci.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kullanıcı oturumunun ve aboneliğinin durumunu
 * istemciye bildirmek için kullanılan DTO.
 * Kill Switch (heartbeat) endpoint'i tarafından döndürülür.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatResponse {

    /**
     * Oturumun ve aboneliğin hala geçerli olup olmadığını belirtir.
     * Eğer bu 'false' ise, istemci (VPN uygulaması) bağlantıyı kesmelidir.
     */
    private boolean active;

    /**
     * Durumun nedenini belirten kısa bir kod (örn: "SESSION_ACTIVE",
     * "NO_ACTIVE_SUBSCRIPTION", "ACCOUNT_LOCKED").
     */
    private String reason;
}
