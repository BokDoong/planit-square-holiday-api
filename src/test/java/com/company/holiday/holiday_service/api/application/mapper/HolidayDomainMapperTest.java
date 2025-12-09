package com.company.holiday.holiday_service.api.application.mapper;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayDomainMapperTest {

    private HolidayDomainMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new HolidayDomainMapper();
    }

    @DisplayName("CountryUpsertCommand를 Country 엔티티로 매핑한다.")
    @Test
    void toCountry() {
        // given
        CountryUpsertCommand command = new CountryUpsertCommand("KR", "Korea, Republic of");

        // when
        Country country = mapper.toCountry(command);

        // then
        assertThat(country.getCode()).isEqualTo("KR");
        assertThat(country.getName()).isEqualTo("Korea, Republic of");
    }

    @DisplayName("HolidayUpsertCommand를 Holiday 엔티티로 매핑한다.")
    @Test
    void toHoliday() {
        // given
        Country country = Country.of("BR", "Brazil");

        HolidayUpsertCommand command = new HolidayUpsertCommand(
                "BR",
                LocalDate.of(2025, 3, 3),
                "Carnaval",
                "Carnival",
                true,          // global
                false,         // fixed
                1990,
                List.of("Bank", "Optional"),
                List.of("BR-11", "BR-26")
        );

        // when
        Holiday holiday = mapper.toHoliday(command, country);

        // then
        // 기본 필드 매핑 확인
        assertThat(holiday.getCountry()).isSameAs(country);
        assertThat(holiday.getDate()).isEqualTo(LocalDate.of(2025, 3, 3));
        assertThat(holiday.getLocalName()).isEqualTo("Carnaval");
        assertThat(holiday.getName()).isEqualTo("Carnival");
        assertThat(holiday.isGlobal()).isTrue();
        assertThat(holiday.isFixed()).isFalse();
        assertThat(holiday.getLaunchYear()).isEqualTo(1990);

        // types → "Bank,Optional" 으로 join 되었는지
        assertThat(holiday.getTypesRaw()).isEqualTo("Bank,Optional");

        // counties → JSON 문자열로 직렬화 되었는지
        assertThat(holiday.getCountiesRaw())
                .isEqualTo("BR-11,BR-26");
    }

    @DisplayName("HolidayUpsertCommand의 types나 counties가 null/empty일 떄 빈 문자열이나 Null로 매핑된다.")
    @Test
    void toHoliday_null_혹은_empty_컬렉션을_적절히_처리한다() {
        // given
        Country country = Country.of("BR", "Brazil");

        HolidayUpsertCommand commandWithNulls = new HolidayUpsertCommand(
                "BR",
                LocalDate.of(2025, 1, 1),
                "New Year's Day",
                "New Year's Day",
                true,
                false,
                null,
                null,
                null
        );

        HolidayUpsertCommand commandWithEmptyLists = new HolidayUpsertCommand(
                "BR",
                LocalDate.of(2025, 1, 2),
                "Dummy",
                "Dummy",
                true,
                false,
                null,
                List.of(),
                List.of()
        );

        // when
        Holiday holidayNulls = mapper.toHoliday(commandWithNulls, country);
        Holiday holidayEmpty = mapper.toHoliday(commandWithEmptyLists, country);

        // then
        // launchYear
        assertThat(holidayNulls.getLaunchYear()).isNull();
        assertThat(holidayEmpty.getLaunchYear()).isNull();

        // types
        assertThat(holidayNulls.getTypesRaw()).isNull();
        assertThat(holidayEmpty.getTypesRaw()).isNull();

        // counties
        assertThat(holidayNulls.getCountiesRaw()).isNull();
        assertThat(holidayEmpty.getCountiesRaw()).isNull();
    }

}
