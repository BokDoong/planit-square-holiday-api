package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.IntegrationTestSupport;
import com.company.holiday.holiday_service.api.domain.Country;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

class CountryRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private CountryRepository countryRepository;

    @DisplayName("나라 코드로 나라의 존재 유무를 확인한다.")
    @Test
    void existsByCode() {
        // given
        Country country = Country.of("KR", "대한민국");
        countryRepository.save(country);

        // when
        Boolean exists = countryRepository.existsByCode(country.getCode());
        Boolean notExists = countryRepository.existsByCode("JP");

        // then
        Assertions.assertThat(exists).isTrue();
        Assertions.assertThat(notExists).isFalse();
    }

    @DisplayName("나라 코드로 나라를 찾는다.")
    @Test
    void findByCode() {
        // given
        Country country = Country.of("KR", "대한민국");
        countryRepository.save(country);

        // when
        Country savedCountry = countryRepository.findByCode(country.getCode()).get();

        // then
        Assertions.assertThat(savedCountry.getCode()).isEqualTo(country.getCode());
        Assertions.assertThat(savedCountry.getName()).isEqualTo(country.getName());
    }

    @DisplayName("나라 코드를 기준으로 오름차순 정렬하여 전체 국가 목록을 조회한다.")
    @Test
    void findAllByOrderByCodeAsc() {
        // given
        Country kr = Country.of("KR", "대한민국");
        Country us = Country.of("US", "미국");
        Country jp = Country.of("JP", "일본");

        countryRepository.save(kr);
        countryRepository.save(us);
        countryRepository.save(jp);

        // when
        List<Country> countries = countryRepository.findAllByOrderByCodeAsc();

        // then
        Assertions.assertThat(countries).hasSize(3);

        // 코드 순서: JP → KR → US
        Assertions.assertThat(countries.get(0).getCode()).isEqualTo("JP");
        Assertions.assertThat(countries.get(1).getCode()).isEqualTo("KR");
        Assertions.assertThat(countries.get(2).getCode()).isEqualTo("US");
    }

}
