package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.CountrySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryQueryService {

    private final CountryRepository countryRepository;

    public List<CountrySearchResponse> searchAll() {
        return countryRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(c -> new CountrySearchResponse(c.getCode(), c.getName()))
                .toList();
    }

}
