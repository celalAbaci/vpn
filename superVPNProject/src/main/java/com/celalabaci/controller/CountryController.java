package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.country.CountryDto;
import com.celalabaci.dto.country.CountryCreateUpdateDto;
import com.celalabaci.service.ICountryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {

    @Autowired
    private ICountryService countryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<List<CountryDto>>> getAllCountries() {
        List<CountryDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PREMIUM')")
    public ResponseEntity<ApiResponse<CountryDto>> getCountryById(@PathVariable Long id) {
        CountryDto country = countryService.getCountryById(id);
        return ResponseEntity.ok(ApiResponse.success(country));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDto>> createCountry(@Valid @RequestBody CountryCreateUpdateDto dto) {
        CountryDto createdCountry = countryService.createCountry(dto);
        return new ResponseEntity<>(ApiResponse.success("Country created successfully.", createdCountry), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryDto>> updateCountry(@PathVariable Long id, @Valid @RequestBody CountryCreateUpdateDto dto) {
        CountryDto updatedCountry = countryService.updateCountry(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Country updated successfully.", updatedCountry));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.ok(ApiResponse.success("Country deleted successfully.", null));
    }
}
