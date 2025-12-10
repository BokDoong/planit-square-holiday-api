package com.company.holiday.holiday_service.api.application.mapper;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HolidayCommandMapper {

    public CountryUpsertCommand toCommand(NagerAvailableCountryResponse dto) {
        return new CountryUpsertCommand(
                dto.countryCode(),
                dto.name()
        );
    }

    public HolidayUpsertCommand toCommand(NagerPublicHolidayResponse dto) {
        return new HolidayUpsertCommand(
                dto.countryCode(),
                dto.date(),
                dto.localName(),
                dto.name(),
                dto.global(),
                dto.fixed(),
                dto.launchYear(),
                normalizeTypes(dto.types()),
                dto.counties()
        );
    }

    private List<String> normalizeTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }
        return List.copyOf(types);
    }

}
