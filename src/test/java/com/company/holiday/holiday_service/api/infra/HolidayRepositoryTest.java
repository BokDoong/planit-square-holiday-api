package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.IntegrationTestSupport;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

        int deletedCount = holidayRepository.deleteByCountryAndDateBetween(kr, start, end);

        // then
        assertThat(deletedCount).isEqualTo(2);

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

    @DisplayName("국가 코드와 날짜 범위로 공휴일을 페이지네이션하여 조회한다.")
    @Test
    void findByCountryCodeAndDateBetween_withPaging() {
        // given
        Country kr = countryRepository.save(Country.of("KR", "Korea, Republic of"));
        Country us = countryRepository.save(Country.of("US", "United States"));

        Holiday krJan1 = holidayRepository.save(createHoliday(
                kr,
                LocalDate.of(2025, 1, 1),
                "새해",
                "New Year",
                null
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
        // 다른 나라(US)는 조회 대상에서 제외되어야 함
        Holiday usJan5 = holidayRepository.save(createHoliday(
                us,
                LocalDate.of(2025, 1, 5),
                "US Holiday",
                "US Holiday",
                null
        ));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        // 0번 페이지, size=2, date 오름차순 정렬
        Pageable pageable = PageRequest.of(0, 2, Sort.by("date").ascending());

        // when
        Page<Holiday> page = holidayRepository.findByCountry_CodeAndDateBetween(
                "KR",
                start,
                end,
                pageable
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        // 정렬 및 필터 확인
        assertThat(page.getContent())
                .extracting(Holiday::getId)
                .containsExactly(krJan1.getId(), krJan10.getId());

        // 모두 KR이고, 날짜는 1월 내여야 한다
        assertThat(page.getContent())
                .allSatisfy(h -> {
                    assertThat(h.getCountry().getCode()).isEqualTo("KR");
                    assertThat(h.getDate()).isBetween(start, end);
                });
    }

    @DisplayName("해당 국가 코드 또는 날짜 범위에 공휴일이 없으면 비어있는 페이지를 반환한다.")
    @Test
    void findByCountryCodeAndDateBetween_emptyResult() {
        // given
        Country kr = countryRepository.save(Country.of("KR", "Korea, Republic of"));

        // 범위 밖의 데이터 (2024년)
        holidayRepository.save(createHoliday(
                kr,
                LocalDate.of(2024, 12, 31),
                "작년",
                "Last Year",
                null
        ));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending());

        // when
        Page<Holiday> resultForOtherCountry = holidayRepository.findByCountry_CodeAndDateBetween(
                "US",
                start,
                end,
                pageable
        );
        Page<Holiday> resultForEmptyRange = holidayRepository.findByCountry_CodeAndDateBetween(
                "KR",
                start,
                end,
                pageable
        );

        // then
        assertThat(resultForOtherCountry.getTotalElements()).isZero();
        assertThat(resultForOtherCountry.getContent()).isEmpty();

        assertThat(resultForEmptyRange.getTotalElements()).isZero();
        assertThat(resultForEmptyRange.getContent()).isEmpty();
    }

    @DisplayName("국가 코드, 날짜 범위, 타입 토큰으로 공휴일을 페이지네이션하여 조회한다.")
    @Test
    void findByCountryCodeAndDateBetweenAndTypesRawContaining_withPagingAndTypeFilter() {
        // given
        Country kr = countryRepository.save(Country.of("KR", "Korea, Republic of"));
        Country us = countryRepository.save(Country.of("US", "United States"));

        // KR - Public
        Holiday krJan1Public = holidayRepository.save(createHolidayWithTypes(
                kr,
                LocalDate.of(2025, 1, 1),
                "새해",
                "New Year",
                "Public"
        ));
        // KR - Bank (필터 대상 아님)
        Holiday krJan10Bank = holidayRepository.save(createHolidayWithTypes(
                kr,
                LocalDate.of(2025, 1, 10),
                "테스트1",
                "Test1",
                "Bank"
        ));
        // KR - Public,School (Public 포함 → 필터 대상)
        Holiday krJan15PublicSchool = holidayRepository.save(createHolidayWithTypes(
                kr,
                LocalDate.of(2025, 1, 15),
                "테스트2",
                "Test2",
                "Public,School"
        ));
        // 범위 밖 (2월) - Public (날짜 조건에서 제외)
        Holiday krFeb1Public = holidayRepository.save(createHolidayWithTypes(
                kr,
                LocalDate.of(2025, 2, 1),
                "테스트3",
                "Test3",
                "Public"
        ));
        // 다른 나라(US)는 조회 대상에서 제외되어야 함
        Holiday usJan5Public = holidayRepository.save(createHolidayWithTypes(
                us,
                LocalDate.of(2025, 1, 5),
                "US Holiday",
                "US Holiday",
                "Public"
        ));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending());

        // when
        Page<Holiday> page = holidayRepository.findByCountry_CodeAndDateBetweenAndTypesRawContaining(
                "KR",
                start,
                end,
                "Public",
                pageable
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        // 정렬 및 필터 확인
        assertThat(page.getContent())
                .extracting(Holiday::getId)
                .containsExactly(
                        krJan1Public.getId(),
                        krJan15PublicSchool.getId()
                );

        assertThat(page.getContent())
                .allSatisfy(h -> {
                    assertThat(h.getCountry().getCode()).isEqualTo("KR");
                    assertThat(h.getDate()).isBetween(start, end);
                    assertThat(h.getTypesRaw()).contains("Public");
                });
    }

    @DisplayName("해당 타입 토큰에 해당하는 공휴일이 없으면 비어있는 페이지를 반환한다.")
    @Test
    void findByCountryCodeAndDateBetweenAndTypesRawContaining_emptyResultForTypeToken() {
        // given
        Country kr = countryRepository.save(Country.of("KR", "Korea, Republic of"));

        // KR - Public만 존재 (Optional 타입은 없음)
        holidayRepository.save(createHolidayWithTypes(
                kr,
                LocalDate.of(2025, 1, 1),
                "새해",
                "New Year",
                "Public"
        ));

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("date").ascending());

        // when
        Page<Holiday> result = holidayRepository.findByCountry_CodeAndDateBetweenAndTypesRawContaining(
                "KR",
                start,
                end,
                "Optional",   // 존재하지 않는 타입 토큰
                pageable
        );

        // then
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }

    private Holiday createHolidayWithTypes(
            Country country,
            LocalDate date,
            String localName,
            String name,
            String typesRaw
    ) {
        return Holiday.of(
                country, date, localName, name, true, false,
                null, typesRaw, null
        );
    }

}
