package com.celalabaci.service;

import com.celalabaci.dto.user.HeartbeatResponse;
import com.celalabaci.entity.User;

/**
 * Kullanıcı oturumunun geçerliliğini (heartbeat) kontrol eden servis arayüzü.
 */
public interface IHeartbeatService {

    /**
     * Mevcut kullanıcının hesabının ve aboneliğinin durumunu kontrol eder.
     *
     * @param currentUser JWT'den alınan, kimliği doğrulanmış kullanıcı.
     * @return İstemcinin aktif kalıp kalmayacağını belirten bir HeartbeatResponse nesnesi.
     */
    HeartbeatResponse checkUserStatus(User currentUser);
}
