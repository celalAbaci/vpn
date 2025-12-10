package com.abacicelal.supervpn_project.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * Ülke model sınıfı.
 * Backend'deki `com.celalabaci.dto.country.CountryDto` ile eşleşir.
 */
public class Country {

    @SerializedName("id")
    private Long id;

    @SerializedName("countryName")
    private String countryName;

    @SerializedName("countryCode")
    private String countryCode;

    // Getter
    public Long getId() {
        return id;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
