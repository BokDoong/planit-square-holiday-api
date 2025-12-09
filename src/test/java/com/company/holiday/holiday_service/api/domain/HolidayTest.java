package com.company.holiday.holiday_service.api.domain;

import com.company.holiday.holiday_service.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HolidayTest {

    @DisplayName("typesRaw가 'Public,Bank'이면 HolidayType 리스트로 변환된다")
    @Test
    void convertTypesRawToEnumList() {
        // given
        Country country = Country.of("KR", "Korea, Republic of");
        Holiday holiday = Holiday.of(
                country,
                LocalDate.of(2025, 1, 1),
                "설날",
                "Lunar New Year",
                true,
                false,
                1990,
                "Public,Bank",
                null
        );

        // when
        List<HolidayType> types = holiday.getTypes();

        // then
        assertThat(types).containsExactly(HolidayType.PUBLIC, HolidayType.BANK);
    }

    @DisplayName("year가 범위를 벗어났다면 InvalidValueException 예외를 던진다.")
    @Test
    void verifyYearInRecentFiveYears_outOfRange() {
        // given
        int invalidYear = 2030;

        // when & then
        assertThatThrownBy(() -> Holiday.verifyYearInRecentFiveYears(invalidYear))
                .isInstanceOf(InvalidValueException.class)
                .hasMessageContaining("year=" + invalidYear)
                .hasMessageContaining("허용 범위: 2021~2025");
    }

    @DisplayName("최근 5년간 연도인지 검증한다.")
    @Test
    void verifyYearInRecentFiveYears() {
        // given
        int validYear = 2023;

        // when
        boolean result = Holiday.verifyYearInRecentFiveYears(validYear);

        // then
        assertThat(result).isTrue();
    }

}
