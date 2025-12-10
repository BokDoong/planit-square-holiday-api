package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.CountrySearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CountryQueryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryQueryService countryQueryService;

    @DisplayName("전체 국가 목록을 코드 오름차순 기준으로 조회한다.")
    @Test
    void searchAll() {
        // given
        Country kr = Country.of("KR", "대한민국");
        Country jp = Country.of("JP", "일본");
        Country us = Country.of("US", "미국");

        given(countryRepository.findAllByOrderByCodeAsc())
                .willReturn(List.of(jp, kr, us));

        // when
        List<CountrySearchResponse> result = countryQueryService.searchAll();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).code()).isEqualTo("JP");
        assertThat(result.get(1).code()).isEqualTo("KR");
        assertThat(result.get(2).code()).isEqualTo("US");

        assertThat(result.get(0).name()).isEqualTo("일본");
        assertThat(result.get(1).name()).isEqualTo("대한민국");
        assertThat(result.get(2).name()).isEqualTo("미국");
    }

}
