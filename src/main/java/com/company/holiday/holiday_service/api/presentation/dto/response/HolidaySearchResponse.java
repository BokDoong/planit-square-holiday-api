package com.company.holiday.holiday_service.api.presentation.dto.response;

import com.company.holiday.holiday_service.api.domain.HolidayType;

import java.time.LocalDate;
import java.util.List;

public record HolidaySearchResponse(
        Long id,
        LocalDate date,
        String localName,
        String name,
        boolean global,
        boolean fixed,
        Integer launchYear,
        List<HolidayType> types,
        List<String> counties
) {}
