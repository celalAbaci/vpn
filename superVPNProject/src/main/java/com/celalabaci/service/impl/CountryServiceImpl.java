package com.celalabaci.service.impl;

import com.celalabaci.dto.country.CountryDto;
import com.celalabaci.dto.country.CountryCreateUpdateDto;
import com.celalabaci.entity.Country;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.CountryMapper;
import com.celalabaci.repository.CountryRepository;
import com.celalabaci.service.ICountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountryServiceImpl implements ICountryService {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryMapper countryMapper;

    @Override
    public List<CountryDto> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(countryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CountryDto getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Country with id " + id + " not found."));
        return countryMapper.toDto(country);
    }

    @Override
    public CountryDto createCountry(CountryCreateUpdateDto dto) {
        if (countryRepository.existsByCountryNameIgnoreCase(dto.getCountryName())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "Country with name '" + dto.getCountryName() + "' already exists.");
        }
        if (countryRepository.existsByCountryCodeIgnoreCase(dto.getCountryCode())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "Country with code '" + dto.getCountryCode() + "' already exists.");
        }
        Country country = countryMapper.toEntity(dto);
        Country savedCountry = countryRepository.save(country);
        return countryMapper.toDto(savedCountry);
    }

    @Override
    public CountryDto updateCountry(Long id, CountryCreateUpdateDto dto) {
        Country existingCountry = countryRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Country with id " + id + " not found."));

        countryMapper.updateEntityFromDto(dto, existingCountry);
        Country updatedCountry = countryRepository.save(existingCountry);
        return countryMapper.toDto(updatedCountry);
    }

    @Override
    public void deleteCountry(Long id) {
        if (!countryRepository.existsById(id)) {
            throw new BaseException(MessageType.NO_RECORD_EXIST, "Country with id " + id + " not found.");
        }
        countryRepository.deleteById(id);
    }
}
