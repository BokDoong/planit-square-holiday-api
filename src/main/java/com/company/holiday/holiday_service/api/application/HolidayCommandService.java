package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.domain.Holiday;
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

import static com.company.holiday.holiday_service.api.domain.HolidayYearRangeCalculator.lastFiveYears;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayCommandService {

    private final CountryRepository countryRepository;

    private final NagerClient nagerClient;
    private final HolidayCommandMapper mapper;

    private final CountrySyncService countrySyncService;
    private final HolidaySyncService holidaySyncService;

    // 최근 5년 동기화
    public HolidaySyncResponse syncCountriesAndHolidays() {
        // 1) 나라 목록 Fetch & Upsert
        List<CountryUpsertCommand> countryCommands = fetchCountries();
        countrySyncService.upsertCountries(countryCommands);
        log.info("[HolidaySync] 국가 동기화 완료 - countriesCount={}", countryCommands.size());

        // 2) 최근 5년 범위 계산
        LocalDate startDate = LocalDate.of(lastFiveYears().fromYear(), 1, 1);
        LocalDate endDate   = LocalDate.of(lastFiveYears().toYear(),   12, 31);

        // 3) 각 나라별로 공휴일 Fetch & Upsert
        int totalSyncedHolidays = syncHolidays(countryCommands, startDate, endDate);

        log.info("[HolidaySync] 전체 동기화 완료 - 국가 수={}, 공휴일 수={}", countryCommands.size(), totalSyncedHolidays);
        return new HolidaySyncResponse(countryCommands.size(), totalSyncedHolidays);
    }

    private List<CountryUpsertCommand> fetchCountries() {
        return nagerClient.getAvailableCountries().stream()
                .map(mapper::toCommand)
                .toList();
    }

    private int syncHolidays(
            List<CountryUpsertCommand> countryCommands,
            LocalDate startDate,
            LocalDate endDate
    ) {
        int total = 0;
        for (CountryUpsertCommand countryCommand : countryCommands) {
            String countryCode = countryCommand.code();
            log.info("[HolidaySync] {} 국가 {}~{}년 공휴일 동기화 시작", countryCode, startDate.getYear(), endDate.getYear());

            List<HolidayUpsertCommand> commands = fetchHolidaysForYears(countryCode, startDate.getYear(), endDate.getYear());
            int synced = holidaySyncService.upsertHolidaysInRange(countryCode, startDate, endDate, deduplicateByDateAndLocalName(commands));
            total += synced;

            log.info("[HolidaySync] {} 국가 공휴일 동기화 완료 - 저장된 공휴일 개수={}", countryCode, synced);
        }
        return total;
    }

    private List<HolidayUpsertCommand> fetchHolidaysForYears(String countryCode, int fromYear, int toYear) {
        List<HolidayUpsertCommand> result = new ArrayList<>();
        for (int year = fromYear; year <= toYear; year++) {
            result.addAll(fetchHolidays(countryCode, year));
        }
        return result;
    }

    private List<HolidayUpsertCommand> fetchHolidays(String countryCode, int year) {
        return nagerClient.getPublicHolidays(year, countryCode).stream()
                .map(mapper::toCommand)
                .toList();
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

    // 특정 나라, 년도의 공휴일 재동기화
    public HolidayRefreshResponse refreshHolidays(int year, String countryCode) {
        verifyCountryIsExist(countryCode);
        Holiday.verifyYearInRecentFiveYears(year);

        List<HolidayUpsertCommand> commands = fetchHolidays(countryCode, year);

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        int reSynced = holidaySyncService.upsertHolidaysInRange(countryCode, start, end, deduplicateByDateAndLocalName(commands));

        log.info("[HolidaySync] {} 국가 {}년 공휴일 재동기화 완료 - 저장된 공휴일 개수={}", countryCode, year, reSynced);
        return new HolidayRefreshResponse(reSynced);
    }

    // 특정 나라, 년도의 공휴일 삭제
    public int deleteHolidays(int year, String countryCode) {
        Holiday.verifyYearInRecentFiveYears(year);
        return holidaySyncService.deleteOneYearHolidays(countryCode, year);
    }

    private void verifyCountryIsExist(String countryCode) {
        if (!countryRepository.existsByCode(countryCode)) {
            throw new EntityNotFoundException(
                    ErrorCode.ENTITY_NOT_FOUND,
                    "존재하지 않는 국가 코드입니다. countryCode=" + countryCode
            );
        }
    }
}
