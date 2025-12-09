package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.domain.Country;
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

    private static final int SYNC_START_YEAR = 2021;
    private static final int SYNC_END_YEAR = 2025;

    @Transactional
    public void upsertRecentFiveYearsHolidays(String countryCode, List<HolidayUpsertCommand> commands) {
        Country country = findCountry(countryCode);
        deleteHolidaysForRecentFiveYears(country);
        holidayRepository.saveAll(mapper.toHolidays(commands, country));
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
                LocalDate.of(SYNC_START_YEAR, 1, 1),
                LocalDate.of(SYNC_END_YEAR, 12, 31)
        );
    }

}
