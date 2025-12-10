package com.company.holiday.holiday_service.api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayYearRangeCalculatorTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @DisplayName("recentYears(n)는 KST 기준 현재 연도로부터 n년 전까지의 범위를 계산한다")
    @Test
    void recentYears() {
        // given
        int years = 3;
        int thisYear = LocalDate.now(KST).getYear();

        // when
        HolidayYearRangeCalculator.YearRange result =
                HolidayYearRangeCalculator.recentYears(years);

        // then
        assertThat(result.fromYear()).isEqualTo(thisYear - years + 1);
        assertThat(result.toYear()).isEqualTo(thisYear);
    }

    @DisplayName("lastFiveYears()는 recentYears(5)와 동일한 범위를 리턴한다")
    @Test
    void lastFiveYears() {
        // given
        int thisYear = LocalDate.now(KST).getYear();

        // when
        HolidayYearRangeCalculator.YearRange result =
                HolidayYearRangeCalculator.lastFiveYears();

        // then
        assertThat(result.fromYear()).isEqualTo(thisYear - 5 + 1);
        assertThat(result.toYear()).isEqualTo(thisYear);
    }

    @DisplayName("lastTwoYears()는 recentYears(2)와 동일한 범위를 리턴한다")
    @Test
    void lastTwoYears() {
        // given
        int thisYear = LocalDate.now(KST).getYear();

        // when
        HolidayYearRangeCalculator.YearRange result =
                HolidayYearRangeCalculator.lastTwoYears();

        // then
        assertThat(result.fromYear()).isEqualTo(thisYear - 2 + 1);
        assertThat(result.toYear()).isEqualTo(thisYear);
    }
}
