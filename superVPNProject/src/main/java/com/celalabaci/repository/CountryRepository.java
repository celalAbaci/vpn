package com.celalabaci.repository;

import com.celalabaci.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    boolean existsByCountryNameIgnoreCase(String countryName);
    boolean existsByCountryCodeIgnoreCase(String countryCode);
}
