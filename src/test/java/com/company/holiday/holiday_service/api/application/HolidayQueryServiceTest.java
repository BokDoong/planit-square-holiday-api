package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidaySearchQuery;
import com.company.holiday.holiday_service.api.application.mapper.HolidayQueryMapper;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.domain.HolidaySyncRange;
import com.company.holiday.holiday_service.api.infra.HolidayRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HolidayQueryServiceTest {

    @Mock
    HolidayRepository holidayRepository;

    @Mock
    HolidayQueryMapper holidayQueryMapper;

    HolidayQueryService holidayQueryService;

    @BeforeEach
    void setUp() {
        holidayQueryService = new HolidayQueryService(holidayRepository, holidayQueryMapper);
    }

    @DisplayName("year가 주어지면 해당 연도의 1/1~12/31 사이 공휴일을 페이징 조회한다")
    @Test
    void search_withYear() {
        // given
        String countryCode = "KR";
        int year = 2023;
        HolidaySearchQuery query = new HolidaySearchQuery(
                countryCode,
                year,
                null,
                null,
                null
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending());

        Holiday h1 = mock(Holiday.class);
        Holiday h2 = mock(Holiday.class);
        Page<Holiday> holidayPage = new PageImpl<>(List.of(h1, h2), pageable, 2);

        LocalDate expectedStart = LocalDate.of(year, 1, 1);
        LocalDate expectedEnd   = LocalDate.of(year, 12, 31);

        given(holidayRepository.findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        )).willReturn(holidayPage);

        HolidaySearchResponse r1 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());
        HolidaySearchResponse r2 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());

        given(holidayQueryMapper.toResponse(h1)).willReturn(r1);
        given(holidayQueryMapper.toResponse(h2)).willReturn(r2);

        // when
        Page<HolidaySearchResponse> result = holidayQueryService.search(query, pageable);

        // then
        verify(holidayRepository).findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        );

        verify(holidayQueryMapper).toResponse(h1);
        verify(holidayQueryMapper).toResponse(h2);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(r1, r2);
    }

    @DisplayName("from/to가 주어지면 해당 범위로 필터링해 공휴일을 페이징 조회한다 (경계 포함)")
    @Test
    void search_withFromAndTo() {
        // given
        String countryCode = "JP";
        LocalDate from = LocalDate.of(2022, 1, 10);
        LocalDate to   = LocalDate.of(2022, 2, 1);

        HolidaySearchQuery query = new HolidaySearchQuery(
                countryCode,
                null,
                from,
                to,
                null
        );

        Pageable pageable = PageRequest.of(0, 5, Sort.by("date").ascending());

        Holiday h1 = mock(Holiday.class);
        Page<Holiday> holidayPage =
                new PageImpl<>(List.of(h1), pageable, 1);

        given(holidayRepository.findByCountry_CodeAndDateBetween(
                countryCode,
                from,
                to,
                pageable
        )).willReturn(holidayPage);

        HolidaySearchResponse r1 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());
        given(holidayQueryMapper.toResponse(h1)).willReturn(r1);

        // when
        Page<HolidaySearchResponse> result = holidayQueryService.search(query, pageable);

        // then
        verify(holidayRepository).findByCountry_CodeAndDateBetween(
                countryCode,
                from,
                to,
                pageable
        );
        verify(holidayQueryMapper).toResponse(h1);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(r1);
    }

    @DisplayName("from/to/year가 모두 없으면 최근 5년 전체(START_YEAR~END_YEAR)를 조회한다")
    @Test
    void search_withoutYearAndRange_defaultRecentFiveYears() {
        // given
        String countryCode = "US";
        HolidaySearchQuery query = new HolidaySearchQuery(
                countryCode,
                null,
                null,
                null,
                null
        );

        Pageable pageable = PageRequest.of(0, 20, Sort.by("date").ascending());

        Holiday h1 = mock(Holiday.class);
        Holiday h2 = mock(Holiday.class);
        Holiday h3 = mock(Holiday.class);
        Page<Holiday> holidayPage =
                new PageImpl<>(List.of(h1, h2, h3), pageable, 3);

        LocalDate expectedStart = LocalDate.of(HolidaySyncRange.START_YEAR, 1, 1);
        LocalDate expectedEnd   = LocalDate.of(HolidaySyncRange.END_YEAR, 12, 31);

        given(holidayRepository.findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        )).willReturn(holidayPage);

        HolidaySearchResponse r1 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());
        HolidaySearchResponse r2 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());
        HolidaySearchResponse r3 = new HolidaySearchResponse(null, null, null, null, null, false, false, null, List.of(), List.of());

        given(holidayQueryMapper.toResponse(h1)).willReturn(r1);
        given(holidayQueryMapper.toResponse(h2)).willReturn(r2);
        given(holidayQueryMapper.toResponse(h3)).willReturn(r3);

        // when
        Page<HolidaySearchResponse> result = holidayQueryService.search(query, pageable);

        // then
        verify(holidayRepository).findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).containsExactly(r1, r2, r3);
    }

    @DisplayName("from만 주어지면 from ~ END_YEAR-12-31 범위로 조회한다")
    @Test
    void search_withFromOnly() {
        // given
        String countryCode = "FR";
        LocalDate from = LocalDate.of(2022, 5, 1);

        HolidaySearchQuery query = new HolidaySearchQuery(
                countryCode,
                null,
                from,
                null,
                null
        );

        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending());

        Holiday h1 = mock(Holiday.class);
        Page<Holiday> holidayPage = new PageImpl<>(List.of(h1), pageable, 1);

        LocalDate expectedStart = from;
        LocalDate expectedEnd   = LocalDate.of(HolidaySyncRange.END_YEAR, 12, 31);

        given(holidayRepository.findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        )).willReturn(holidayPage);

        HolidaySearchResponse r1 = new HolidaySearchResponse(
                null, null, null, null, null,
                false, false, null,
                List.of(), List.of()
        );
        given(holidayQueryMapper.toResponse(h1)).willReturn(r1);

        // when
        Page<HolidaySearchResponse> result = holidayQueryService.search(query, pageable);

        // then
        verify(holidayRepository).findByCountry_CodeAndDateBetween(
                countryCode,
                expectedStart,
                expectedEnd,
                pageable
        );
        verify(holidayQueryMapper).toResponse(h1);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(r1);
    }

}
