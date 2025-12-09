package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.domain.HolidaySyncRange;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySyncResponse;
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

    // 전체 국가 + 최근 5년 공휴일 일괄 동기화
    public HolidaySyncResponse syncCountriesAndHolidays() {
        List<CountryUpsertCommand> countryCommands = fetchCountries();
        countrySyncService.upsertCountries(countryCommands);
        log.info("[HolidaySync] 국가 동기화 완료 - countriesCount={}", countryCommands.size());

        int totalHolidayCount = syncHolidaysForCountries(countryCommands);

        log.info("[HolidaySync] 전체 동기화 완료 - 국가 수 ={}, 공휴일 수 ={}", countryCommands.size(), totalHolidayCount);
        return new HolidaySyncResponse(countryCommands.size(), totalHolidayCount);
    }

    // 특정 국가/연도 재동기화
    public HolidayRefreshResponse refreshHolidays(int year, String countryCode) {
        verifyCountryExists(countryCode);
        Holiday.verifyYearInRecentFiveYears(year);

        List<HolidayUpsertCommand> commands = fetchHolidays(countryCode, year);
        holidaySyncService.upsertOneYearHolidays(countryCode, deduplicateByDateAndLocalName(commands), year);

        return new HolidayRefreshResponse(commands.size());
    }

    // 특정 국가/연도 공휴일 삭제
    public int deleteHolidays(Integer year, String countryCode) {
        Holiday.verifyYearInRecentFiveYears(year);
        return holidaySyncService.deleteOneYearHolidays(countryCode, year);
    }

    private int syncHolidaysForCountries(List<CountryUpsertCommand> countryCommands) {
        int total = 0;
        for (CountryUpsertCommand countryCommand : countryCommands) {
            int count = syncRecentFiveYearsHolidays(countryCommand);
            total += count;
            log.info("[HolidaySync] 최근 5년 공휴일 동기화 완료 - 국가 코드={}, 공휴일 개수={}", countryCommand.code(), count);
        }
        return total;
    }

    private int syncRecentFiveYearsHolidays(CountryUpsertCommand countryCommand) {
        List<HolidayUpsertCommand> holidayCommands = new ArrayList<>();
        for (int year = HolidaySyncRange.START_YEAR; year <= HolidaySyncRange.END_YEAR; year++) {
            holidayCommands.addAll(fetchHolidays(countryCommand.code(), year));
        }

        holidaySyncService.upsertRecentFiveYearsHolidays(countryCommand.code(), deduplicateByDateAndLocalName(holidayCommands));
        return holidayCommands.size();
    }

    private List<CountryUpsertCommand> fetchCountries() {
        return nagerClient.getAvailableCountries().stream()
                .map(mapper::toCountryCommand)
                .toList();
    }

    private List<HolidayUpsertCommand> fetchHolidays(String countryCode, int year) {
        return nagerClient.getPublicHolidays(year, countryCode).stream()
                .map(mapper::toHolidayCommand)
                .toList();
    }

    private void verifyCountryExists(String countryCode) {
        if (!countryRepository.existsByCode(countryCode)) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "존재하지 않는 국가 코드입니다. countryCode=" + countryCode);
        }
    }

    List<HolidayUpsertCommand> deduplicateByDateAndLocalName(List<HolidayUpsertCommand> commands) {
        record HolidayKey(LocalDate date, String localName) {}

        Map<HolidayKey, HolidayUpsertCommand> map = new LinkedHashMap<>();
        for (HolidayUpsertCommand c : commands) {
            HolidayKey key = new HolidayKey(c.date(), c.localName());
            map.putIfAbsent(key, c);
        }

        return new ArrayList<>(map.values());
    }

}
