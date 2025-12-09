package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.infra.HolidayRepository;
import com.company.holiday.holiday_service.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidaySyncServiceTest {

    @Mock
    CountryRepository countryRepository;

    @Mock
    HolidayRepository holidayRepository;

    @Mock
    HolidayDomainMapper holidayDomainMapper;

    HolidaySyncService holidaySyncService;

    @BeforeEach
    void setUp() {
        holidaySyncService = new HolidaySyncService(
                countryRepository,
                holidayRepository,
                holidayDomainMapper
        );
    }

    @DisplayName("존재하는 국가의 특정 연도 공휴일을 모두 삭제한 뒤 새로 저장한다")
    @Test
    void upsertOneYear_Holidays_deletePreviousAndSaveNew() {
        // given
        String countryCode = "KR";
        int year = 2025;

        Country country = createMockCountry();
        List<HolidayUpsertCommand> commands = createHolidayUpsertCommands();
        List<Holiday> mappedHolidays = createMockHolidays();

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.of(country));
        given(holidayDomainMapper.toHolidays(commands, country)).willReturn(mappedHolidays);

        // when
        holidaySyncService.upsertOneYearHolidays(countryCode, commands, year);

        // then
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        verify(holidayRepository).deleteByCountryAndDateBetween(country, start, end);
        verify(holidayDomainMapper).toHolidays(commands, country);
        verify(holidayRepository).saveAll(mappedHolidays);
    }

    @DisplayName("국가 코드가 존재하지 않으면 EntityNotFoundException을 던지고 연도별 삭제/저장은 수행하지 않는다")
    @Test
    void upsertOneYear_Holidays_throwWhenCountryNotFound() {
        // given
        String countryCode = "ZZ";
        int year = 2025;

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> holidaySyncService.upsertOneYearHolidays(countryCode, List.of(), year))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("존재하는 국가의 최근 5년 공휴일을 모두 삭제한 뒤 새로 저장한다")
    @Test
    void upsertRecentFiveYears_Holidays_deletePreviousAndSaveNew() {
        // given
        String countryCode = "KR";

        Country country = createMockCountry();
        List<HolidayUpsertCommand> commands = createHolidayUpsertCommands();
        List<Holiday> mappedHolidays = createMockHolidays();

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.of(country));
        given(holidayDomainMapper.toHolidays(commands, country)).willReturn(mappedHolidays);

        // when
        holidaySyncService.upsertRecentFiveYearsHolidays(countryCode, commands);

        // then
        LocalDate start = LocalDate.of(2021, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        verify(holidayRepository).deleteByCountryAndDateBetween(country, start, end);
        verify(holidayDomainMapper).toHolidays(commands, country);
        verify(holidayRepository).saveAll(mappedHolidays);
    }

    @DisplayName("국가 코드가 존재하지 않으면 EntityNotFoundException을 던지고 최근 5년 삭제/저장은 수행하지 않는다")
    @Test
    void upsertRecentFiveYears_Holidays_throwWhenCountryNotFound() {
        // given
        String countryCode = "ZZ";

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> holidaySyncService.upsertRecentFiveYearsHolidays(countryCode, List.of()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Country createMockCountry() {
        return mock(Country.class);
    }

    private List<HolidayUpsertCommand> createHolidayUpsertCommands() {
        HolidayUpsertCommand command1 = new HolidayUpsertCommand(
                "KR",
                LocalDate.of(2025, 1, 1),
                "설날",
                "Lunar New Year",
                true,
                false,
                1990,
                List.of("Public"),
                List.of("KR-11")
        );
        HolidayUpsertCommand command2 = new HolidayUpsertCommand(
                "KR",
                LocalDate.of(2025, 3, 1),
                "삼일절",
                "Independence Movement Day",
                true,
                false,
                null,
                List.of("Public"),
                null
        );
        return List.of(command1, command2);
    }

    private List<Holiday> createMockHolidays() {
        return List.of(
                mock(Holiday.class),
                mock(Holiday.class)
        );
    }

}
