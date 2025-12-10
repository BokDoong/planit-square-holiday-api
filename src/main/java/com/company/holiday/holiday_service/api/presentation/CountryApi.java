package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.api.application.CountryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
public class CountryApi {

    private final CountryQueryService countryQueryService;

    @GetMapping
    public List<CountryResponse> getAllCountries() {
        return countryQueryService.findAllCountries();
    }

}
