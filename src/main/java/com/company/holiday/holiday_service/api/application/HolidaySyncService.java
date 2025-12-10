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

    @Transactional
    public int upsertHolidaysInRange(String countryCode, LocalDate start, LocalDate end, List<HolidayUpsertCommand> commands) {
        Country country = findCountry(countryCode);
        deleteHolidaysInRange(country, start, end);
        return holidayRepository.saveAll(mapper.toHolidays(commands, country)).size();
    }

    private Country findCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "존재하지 않는 국가 코드입니다. countryCode=" + countryCode
                ));
    }

    private int deleteHolidaysInRange(Country country, LocalDate start, LocalDate end) {
        return holidayRepository.deleteInRange(country, start, end);
    }

    @Transactional
    public int deleteOneYearHolidays(String countryCode, int year) {
        Country country = findCountry(countryCode);
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end   = LocalDate.of(year, 12, 31);
        return deleteHolidaysInRange(country, start, end);
    }

}
