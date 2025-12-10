package com.company.holiday.holiday_service.api.application.dto;

import com.company.holiday.holiday_service.api.domain.HolidayType;

import java.time.LocalDate;

public record HolidaySearchQuery(
        String countryCode,
        Integer year,
        LocalDate from,
        LocalDate to,
        HolidayType type
) { }
