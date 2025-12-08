package com.celalabaci.service;

import com.celalabaci.dto.country.CountryDto;
import com.celalabaci.dto.country.CountryCreateUpdateDto;
import java.util.List;

public interface ICountryService {
    List<CountryDto> getAllCountries();
    CountryDto getCountryById(Long id);
    CountryDto createCountry(CountryCreateUpdateDto dto);
    CountryDto updateCountry(Long id, CountryCreateUpdateDto dto);
    void deleteCountry(Long id);
}
