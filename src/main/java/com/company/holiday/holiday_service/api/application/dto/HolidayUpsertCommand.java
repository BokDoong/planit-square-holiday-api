package com.company.holiday.holiday_service.api.application.dto;


import java.time.LocalDate;
import java.util.List;

public record HolidayUpsertCommand(
        String countryCode,
        LocalDate date,
        String localName,
        String name,
        boolean global,
        boolean fixed,
        Integer launchYear,
        List<String> types,
        List<String> counties
) {
}
