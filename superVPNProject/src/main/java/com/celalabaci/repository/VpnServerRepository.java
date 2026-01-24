package com.celalabaci.repository;

import com.celalabaci.entity.VpnServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VpnServerRepository extends JpaRepository<VpnServer, Long> {

    List<VpnServer> findByActiveTrue();

    List<VpnServer> findByActiveTrueAndIsFreeTrue();

    boolean existsByServerIpAddressIgnoreCase(String serverIpAddress);

    @Query("SELECT AVG(v.currentLoadPercentage) FROM VpnServer v WHERE v.active = true")
    Double findAverageActiveServerLoad();

    @Query("SELECT SUM(v.currentConnectedUsers) FROM VpnServer v WHERE v.active = true")
    Integer findTotalActiveConnectedUsers();
}
