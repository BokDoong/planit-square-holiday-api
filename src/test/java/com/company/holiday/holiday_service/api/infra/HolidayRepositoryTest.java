package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.IntegrationTestSupport;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private CountryRepository countryRepository;

    @DisplayName("특정 나라의 날짜 구간 안에 있는 모든 공휴일을 삭제한다.")
    @Test
    void deleteByCountryAndDateBetween() {
        // given
        Country kr = countryRepository.save(Country.of("KR", "Korea, Republic of"));

        Holiday krJan1 = holidayRepository.save(createHoliday(
                kr,
                LocalDate.of(2025, 1, 1),
                "새해",
                "New Year",
                1990
        ));
        Holiday krJan10 = holidayRepository.save(createHoliday(
                kr,
                LocalDate.of(2025, 1, 10),
                "테스트1",
                "Test1",
                null
        ));
        Holiday krFeb1 = holidayRepository.save(createHoliday(
                kr,
                LocalDate.of(2025, 2, 1),
                "테스트2",
                "Test2",
                null
        ));

        // when
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        holidayRepository.deleteByCountryAndDateBetween(kr, start, end);

        // then
        List<Holiday> remain = holidayRepository.findAll();
        assertThat(remain)
                .extracting(Holiday::getId)
                .containsExactlyInAnyOrder(
                        krFeb1.getId()
                );

        assertThat(remain).allMatch(h -> (h.getCountry().getCode().equals("KR") && h.getDate().getMonthValue() != 1));
    }

    private Holiday createHoliday(Country country, LocalDate date, String localName, String name, Integer launchYear) {
        return Holiday.of(
                        country, date, localName, name, true, false,
                        launchYear, "Public", null
                );
    }

}
