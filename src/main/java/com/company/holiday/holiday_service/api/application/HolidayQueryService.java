package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.HolidaySearchQuery;
import com.company.holiday.holiday_service.api.application.mapper.HolidayQueryMapper;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.infra.HolidayRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.company.holiday.holiday_service.api.domain.HolidayYearRangeCalculator.*;

@Service
@RequiredArgsConstructor
public class HolidayQueryService {

    private final HolidayRepository holidayRepository;

    private final HolidayQueryMapper mapper;

    @Transactional(readOnly = true)
    public Page<HolidaySearchResponse> search(HolidaySearchQuery query, Pageable pageable) {
        Page<Holiday> holidays = searchHolidays(query, pageable);
        return holidays.map(mapper::toResponse);
    }

    private Page<Holiday> searchHolidays(HolidaySearchQuery query, Pageable pageable) {

        DateRange range = filterDateRange(query);

        // 기본 : 나라, 연도
        if (query.type() == null) {
            return holidayRepository.findByCountry_CodeAndDateBetween(
                    query.countryCode(),
                    range.start(),
                    range.end(),
                    pageable
            );
        }
        // 타입 필터 포함
        else {
            return holidayRepository.findByCountry_CodeAndDateBetweenAndTypesRawContaining(
                    query.countryCode(),
                    range.start(),
                    range.end(),
                    query.type().getValue(),
                    pageable
            );
        }
    }

    private DateRange filterDateRange(HolidaySearchQuery query) {
        LocalDate from = query.from();
        LocalDate to   = query.to();
        Integer year   = query.year();

        // 1) from/to 기반
        if (from != null || to != null) {
            LocalDate start = filterStartDateRange(from);
            LocalDate end   = filterEndDateRange(to);
            return new DateRange(start, end);
        }

        // 2) year 기반
        if (year != null) {
            Holiday.verifyYearInRecentFiveYears(year);
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end   = LocalDate.of(year, 12, 31);
            return new DateRange(start, end);
        }

        // 3) 아무것도 없으면 최근 5년 전체
        return new DateRange(
                LocalDate.of(lastFiveYears().fromYear(), 1, 1),
                LocalDate.of(lastFiveYears().toYear(), 12, 31)
        );
    }

    private LocalDate filterStartDateRange(LocalDate from) {
        if (from == null) {
            return LocalDate.of(lastFiveYears().fromYear(), 1, 1);
        }
        Holiday.verifyYearInRecentFiveYears(from.getYear());
        return from;
    }

    private LocalDate filterEndDateRange(LocalDate to) {
        if (to == null) {
            return LocalDate.of(lastFiveYears().toYear(), 12, 31);
        }
        Holiday.verifyYearInRecentFiveYears(to.getYear());
        return to;
    }

    private record DateRange(LocalDate start, LocalDate end) {}

}
