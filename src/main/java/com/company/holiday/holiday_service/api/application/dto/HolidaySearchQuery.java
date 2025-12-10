package com.company.holiday.holiday_service.api.application.dto;

import java.time.LocalDate;

public record HolidaySearchQuery(
        String countryCode,
        Integer year,
        LocalDate from,
        LocalDate to
) { }
