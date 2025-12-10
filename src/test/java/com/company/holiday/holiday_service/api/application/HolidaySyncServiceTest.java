package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.infra.HolidayRepository;
import com.company.holiday.holiday_service.global.error.ErrorCode;
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

import static org.assertj.core.api.Assertions.assertThat;
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

    @DisplayName("존재하는 국가의 특정 기간 공휴일을 모두 삭제한 뒤 새로 저장하고, 저장 건수를 리턴한다")
    @Test
    void upsertHolidaysInRange_deletePreviousAndSaveNew() {
        // given
        String countryCode = "KR";
        LocalDate start = LocalDate.of(2021, 1, 1);
        LocalDate end   = LocalDate.of(2025, 12, 31);

        Country country = createMockCountry();
        List<HolidayUpsertCommand> commands = createHolidayUpsertCommands();
        List<Holiday> mappedHolidays = createMockHolidays();

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.of(country));
        given(holidayDomainMapper.toHolidays(commands, country)).willReturn(mappedHolidays);
        given(holidayRepository.saveAll(mappedHolidays)).willReturn(mappedHolidays);

        // when
        int result = holidaySyncService.upsertHolidaysInRange(countryCode, start, end, commands);

        // then
        // 1) 기존 기간 공휴일 삭제
        verify(holidayRepository).deleteInRange(country, start, end);
        // 2) 매핑 및 저장
        verify(holidayDomainMapper).toHolidays(commands, country);
        verify(holidayRepository).saveAll(mappedHolidays);
        // 3) 저장된 건수 리턴
        assertThat(result).isEqualTo(mappedHolidays.size());
    }

    @DisplayName("국가 코드가 존재하지 않으면 EntityNotFoundException을 던지고 기간 삭제/저장은 수행하지 않는다")
    @Test
    void upsertHolidaysInRange_throwWhenCountryNotFound() {
        // given
        String countryCode = "ZZ";
        LocalDate start = LocalDate.of(2021, 1, 1);
        LocalDate end   = LocalDate.of(2025, 12, 31);

        given(countryRepository.findByCode(countryCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                holidaySyncService.upsertHolidaysInRange(countryCode, start, end, List.of())
        ).isInstanceOf(EntityNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);

        // 매퍼/리포지토리는 전혀 호출되지 않아야 한다.
        verify(holidayRepository, never()).deleteInRange(any(), any(), any());
        verify(holidayRepository, never()).saveAll(anyList());
        verifyNoInteractions(holidayDomainMapper);
    }

    @DisplayName("특정 국가/연도의 공휴일을 삭제하고, 삭제된 건수를 리턴한다")
    @Test
    void deleteOneYearHolidays_success() {
        // given
        String countryCode = "KR";
        int year = 2023;

        Country country = mock(Country.class);

        given(countryRepository.findByCode(countryCode))
                .willReturn(Optional.of(country));

        int deletedCount = 7;
        given(holidayRepository.deleteInRange(
                eq(country),
                eq(LocalDate.of(year, 1, 1)),
                eq(LocalDate.of(year, 12, 31))
        )).willReturn(deletedCount);

        // when
        int result = holidaySyncService.deleteOneYearHolidays(countryCode, year);

        // then
        // 1) 삭제 쿼리가 올바른 파라미터로 호출되었는지 검증
        verify(holidayRepository).deleteInRange(
                eq(country),
                eq(LocalDate.of(year, 1, 1)),
                eq(LocalDate.of(year, 12, 31))
        );
        // 2) 반환값이 deleteByCountryAndDateBetween 의 결과와 동일한지 검증
        assertThat(result).isEqualTo(deletedCount);
    }

    @DisplayName("연도별 삭제 시 국가 코드가 존재하지 않으면 EntityNotFoundException을 던지고 삭제는 수행하지 않는다")
    @Test
    void deleteOneYearHolidays_throwWhenCountryNotFound() {
        // given
        String countryCode = "ZZ";
        int year = 2023;

        given(countryRepository.findByCode(countryCode))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> holidaySyncService.deleteOneYearHolidays(countryCode, year))
                .isInstanceOf(EntityNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);

        verify(holidayRepository, never()).deleteInRange(any(), any(), any());
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
