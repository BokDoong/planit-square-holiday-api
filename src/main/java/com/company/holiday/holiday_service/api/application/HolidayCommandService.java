package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.presentation.dto.HolidaySyncResponse;
import com.company.holiday.holiday_service.clients.nager.NagerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayCommandService {

    private final NagerClient nagerClient;
    private final HolidayCommandMapper mapper;

    private final CountrySyncService countrySyncService;
    private final HolidaySyncService holidaySyncService;

    private static final int SYNC_START_YEAR = 2021;
    private static final int SYNC_END_YEAR = 2025;

    public HolidaySyncResponse syncCountriesAndHolidays() {

        List<CountryUpsertCommand> countryCommands = fetchCountries();
        log.info("[HolidaySync] 시작 - 가용 국가 수={}개", countryCommands.size());

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

    private List<HolidayUpsertCommand> fetchHolidays(CountryUpsertCommand command, int year) {
        return nagerClient.getPublicHolidays(year, command.code()).stream()
                .map(mapper::toHolidayCommand)
                .toList();
    }

    private int syncRecentFiveYearsHolidays(CountryUpsertCommand countryCommand) {
        List<HolidayUpsertCommand> holidayCommands = new ArrayList<>();
        for (int year = SYNC_START_YEAR; year <= SYNC_END_YEAR; year++) {
            holidayCommands.addAll(fetchHolidays(countryCommand, year));
        }
        holidaySyncService.upsertRecentFiveYearsHolidays(countryCommand.code(), holidayCommands);
        return holidayCommands.size();
    }

}
