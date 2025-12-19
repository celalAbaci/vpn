package com.celalabaci.dto.country;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryCreateUpdateDto {

    @NotEmpty(message = "Country name cannot be empty.")
    @Size(max = 100, message = "Country name must be less than 100 characters.")
    private String countryName;

    @NotEmpty(message = "Country code cannot be empty.")
    @Size(min = 2, max = 10, message = "Country code must be between 2 and 10 characters.")
    private String countryCode;
}
