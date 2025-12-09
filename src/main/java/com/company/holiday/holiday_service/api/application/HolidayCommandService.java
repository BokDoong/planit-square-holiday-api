package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.presentation.dto.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.HolidaySyncResponse;
import com.company.holiday.holiday_service.clients.nager.NagerClient;
import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayCommandService {

    private final CountryRepository countryRepository;

    private final NagerClient nagerClient;
    private final HolidayCommandMapper mapper;

    private final CountrySyncService countrySyncService;
    private final HolidaySyncService holidaySyncService;

    private static final int SYNC_START_YEAR = 2021;
    private static final int SYNC_END_YEAR = 2025;

    public HolidaySyncResponse syncCountriesAndHolidays() {
        List<CountryUpsertCommand> countryCommands = fetchCountries();
        syncCountries(countryCommands);
        log.info("[HolidaySync] 국가 동기화 완료 - countriesCount={}", countryCommands.size());

        int syncedHolidaysCount = 0;
        for (CountryUpsertCommand countryCommand : countryCommands) {
            int count = syncRecentFiveYearsHolidays(countryCommand);
            syncedHolidaysCount += count;
            log.info("[HolidaySync] 최근 5년 공휴일 동기화 완료 - 국가 코드={}, 공휴일 개수={}", countryCommand.code(), count);
        }

        log.info("[HolidaySync] 전체 동기화 완료 - 국가 수 ={}, 공휴일 수 ={}", countryCommands.size(), syncedHolidaysCount);
        return new HolidaySyncResponse(countryCommands.size(), syncedHolidaysCount);
    }

    private List<CountryUpsertCommand> fetchCountries() {
        return nagerClient.getAvailableCountries().stream()
                .map(mapper::toCountryCommand)
                .toList();
    }

    private void syncCountries(List<CountryUpsertCommand> countryCommands) {
        countrySyncService.upsertCountries(countryCommands);
    }

    private int syncRecentFiveYearsHolidays(CountryUpsertCommand countryCommand) {
        List<HolidayUpsertCommand> holidayCommands = new ArrayList<>();
        for (int year = SYNC_START_YEAR; year <= SYNC_END_YEAR; year++) {
            holidayCommands.addAll(fetchHolidays(countryCommand.code(), year));
        }

        holidaySyncService.upsertRecentFiveYearsHolidays(countryCommand.code(), deduplicateByDateAndLocalName(holidayCommands));
        return holidayCommands.size();
    }

    private List<HolidayUpsertCommand> fetchHolidays(String countryCode, int year) {
        return nagerClient.getPublicHolidays(year, countryCode).stream()
                .map(mapper::toHolidayCommand)
                .toList();
    }

    public List<HolidayUpsertCommand> deduplicateByDateAndLocalName(List<HolidayUpsertCommand> commands) {
        record HolidayKey(LocalDate date, String localName) {}

        Map<HolidayKey, HolidayUpsertCommand> map = new LinkedHashMap<>();
        for (HolidayUpsertCommand c : commands) {
            HolidayKey key = new HolidayKey(c.date(), c.localName());
            map.putIfAbsent(key, c);
        }

        return new ArrayList<>(map.values());
    }

    public HolidayRefreshResponse refreshHolidays(int year, String countryCode) {
        verifyCountryIsExist(countryCode);
        Holiday.verifyYearInRecentFiveYears(year);

        List<HolidayUpsertCommand> commands = fetchHolidays(countryCode, year);
        holidaySyncService.upsertOneYearHolidays(countryCode, deduplicateByDateAndLocalName(commands), year);

        return new HolidayRefreshResponse(commands.size());
    }

    private void verifyCountryIsExist(String countryCode) {
        if (!countryRepository.existsByCode(countryCode)) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "존재하지 않는 국가 코드입니다. countryCode=" + countryCode);
        }
    }

}
