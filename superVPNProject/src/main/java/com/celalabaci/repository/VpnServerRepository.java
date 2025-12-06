package com.celalabaci.repository;

import com.celalabaci.entity.VpnServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VpnServerRepository extends JpaRepository<VpnServer, Long> {
    /**
     * Finds all active VPN servers.
     * @return A list of VpnServer entities where isActive is true.
     */
    List<VpnServer> findByIsActiveTrue();

    /**
     * Checks if a server with the given IP address already exists (case-insensitive).
     * @param serverIpAddress The IP address to check.
     * @return true if a server with this IP exists, false otherwise.
     */
    boolean existsByServerIpAddressIgnoreCase(String serverIpAddress);

    // --- YENİ EKLENEN METOTLAR (Dashboard için) ---

    /**
     * Aktif sunucuların ortalama yük yüzdesini hesaplar.
     * Eğer hiç aktif sunucu yoksa NULL döner.
     * @return Ortalama yük (Double) veya null.
     */
    @Query("SELECT AVG(v.currentLoadPercentage) FROM VpnServer v WHERE v.isActive = true")
    Double findAverageActiveServerLoad();

    /**
     * Aktif sunuculardaki toplam bağlı kullanıcı sayısını hesaplar.
     * Eğer hiç aktif sunucu yoksa NULL döner.
     * @return Toplam kullanıcı (Integer) veya null.
     */
    @Query("SELECT SUM(v.currentConnectedUsers) FROM VpnServer v WHERE v.isActive = true")
    Integer findTotalActiveConnectedUsers();
}
