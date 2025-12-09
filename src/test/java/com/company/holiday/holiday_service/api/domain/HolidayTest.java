package com.company.holiday.holiday_service.api.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

}
