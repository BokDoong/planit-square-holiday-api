package com.company.holiday.holiday_service.api.application.mapper;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HolidayDomainMapper {

    public Country toCountry(CountryUpsertCommand command) {
        return Country.of(
                command.code(),
                command.name()
        );
    }

    public List<Holiday> toHolidays(List<HolidayUpsertCommand> commands, Country country) {
        return commands.stream()
                .map(command -> toHoliday(command, country))
                .toList();
    }

    public Holiday toHoliday(HolidayUpsertCommand command, Country country) {
        String typesRaw = joinTypes(command.types());
        String countiesRaw = joinTypes(command.counties());

        return Holiday.of(
                country,
                command.date(),
                command.localName(),
                command.name(),
                command.global(),
                command.fixed(),
                command.launchYear(),
                typesRaw,
                countiesRaw
        );
    }

    // ["Public", "Bank"] → "Public,Bank"
    private String joinTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        return String.join(",", types);
    }

}
