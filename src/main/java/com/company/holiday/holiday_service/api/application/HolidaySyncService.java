package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.HolidaySyncRange;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.infra.HolidayRepository;
import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidaySyncService {

    private final CountryRepository countryRepository;
    private final HolidayRepository holidayRepository;
    private final HolidayDomainMapper mapper;

    @Transactional
    public int upsertRecentFiveYearsHolidays(String countryCode, List<HolidayUpsertCommand> commands) {
        Country country = findCountry(countryCode);
        deleteHolidaysForRecentFiveYears(country);
        return holidayRepository.saveAll(mapper.toHolidays(commands, country)).size();
    }

    private Country findCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "존재하지 않는 국가 코드입니다. countryCode=" + countryCode
                ));
    }

    private void deleteHolidaysForRecentFiveYears(Country country) {
        holidayRepository.deleteByCountryAndDateBetween(
                country,
                LocalDate.of(HolidaySyncRange.START_YEAR, 1, 1),
                LocalDate.of(HolidaySyncRange.END_YEAR, 12, 31)
        );
    }

    @Transactional
    public void upsertOneYearHolidays(String countryCode, List<HolidayUpsertCommand> commands, int year) {
        Country country = findCountry(countryCode);
        deleteHolidaysForYear(country, year);
        holidayRepository.saveAll(mapper.toHolidays(commands, country));
    }

    private int deleteHolidaysForYear(Country country, int year) {
        return holidayRepository.deleteByCountryAndDateBetween(
                country,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
    }

    @Transactional
    public int deleteOneYearHolidays(String countryCode, Integer year) {
        Country country = findCountry(countryCode);
        return deleteHolidaysForYear(country, year);
    }

}
