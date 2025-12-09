package com.company.holiday.holiday_service.api.application.mapper;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayCommandMapperTest {

    private HolidayCommandMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new HolidayCommandMapper();
    }

    @DisplayName("NagerAvailableCountryResponse를 CountryUpsertCommand로 매핑한다.")
    @Test
    void toCountryCommand() {
        // given
        NagerAvailableCountryResponse dto =
                new NagerAvailableCountryResponse("KR", "Korea, Republic of");

        // when
        CountryUpsertCommand command = mapper.toCountryCommand(dto);

        // then
        assertThat(command.code()).isEqualTo("KR");
        assertThat(command.name()).isEqualTo("Korea, Republic of");
    }

    @DisplayName("NagerPublicHolidayResponse를 HolidayUpsertCommand로 매핑한다.")
    @Test
    void toHolidayCommand() {
        // given
        NagerPublicHolidayResponse dto = new NagerPublicHolidayResponse(
                LocalDate.of(2025, 1, 1),
                "설날",
                "Lunar New Year",
                "KR",
                false,
                true,
                List.of("KR-11", "KR-26"),
                1990,
                List.of("Public", "Bank")
        );

        // when
        HolidayUpsertCommand command = mapper.toHolidayCommand(dto);

        // then
        assertThat(command.countryCode()).isEqualTo("KR");
        assertThat(command.date()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(command.localName()).isEqualTo("설날");
        assertThat(command.name()).isEqualTo("Lunar New Year");
        assertThat(command.global()).isTrue();
        assertThat(command.fixed()).isFalse();
        assertThat(command.launchYear()).isEqualTo(1990);

        assertThat(command.types())
                .containsExactlyInAnyOrder("Public", "Bank");

        assertThat(command.counties())
                .containsExactlyInAnyOrder("KR-11", "KR-26");
    }

    @DisplayName("공휴일 타입, 연방주, 공휴일 설립일이 없을 때, HolidayUpsertCommand는 launchYear=null, types=빈 리스트, counties=null로 매핑된다.")
    @Test
    void handleNullOrEmptyFieldsToHolidayCommand() {
        // given
        NagerPublicHolidayResponse dtoWithNulls = new NagerPublicHolidayResponse(
                LocalDate.of(2025, 5, 5),
                "어린이날",
                "Children's Day",
                "KR",
                true,
                true,
                null,      // counties = null
                null,      // launchYear = null
                List.of()  // types = empty
        );

        // when
        HolidayUpsertCommand command = mapper.toHolidayCommand(dtoWithNulls);

        // then
        assertThat(command.launchYear()).isNull();
        assertThat(command.types()).isEmpty();
        assertThat(command.counties()).isNull();
    }
}
