package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.IntegrationTestSupport;
import com.company.holiday.holiday_service.api.domain.Country;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CountryRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private CountryRepository countryRepository;

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

}
