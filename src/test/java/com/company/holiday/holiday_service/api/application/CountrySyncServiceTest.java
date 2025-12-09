package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CountrySyncServiceTest {

    @Mock
    CountryRepository countryRepository;

    @Mock
    HolidayDomainMapper holidayDomainMapper;

    CountrySyncService countrySyncService;

    @BeforeEach
    void setUp() {
        countrySyncService = new CountrySyncService(countryRepository, holidayDomainMapper);
    }

    @DisplayName("존재하지 않는 국가만 새로 저장한다")
    @Test
    void upsertCountries_saveOnlyNewCountries() {
        // given
        CountryUpsertCommand krCommand = new CountryUpsertCommand("KR", "Korea, Republic of");
        CountryUpsertCommand usCommand = new CountryUpsertCommand("US", "United States");

        Country kr = Country.of("KR", "Korea, Republic of");
        Country us = Country.of("US", "United States");

        given(countryRepository.findByCode("KR")).willReturn(Optional.of(kr));
        given(countryRepository.findByCode("US")).willReturn(Optional.empty());
        given(holidayDomainMapper.toCountry(usCommand)).willReturn(us);

        List<CountryUpsertCommand> inputs = List.of(krCommand, usCommand);

        // when
        countrySyncService.upsertCountries(inputs);

        // then
        // 존재하는 KR은 save 호출 X
        verify(countryRepository, never()).save(argThat(c -> c.getCode().equals("KR")));

        // 존재하지 않는 US는 save 호출 O
        verify(countryRepository, times(1)).save(argThat(c ->
                c.getCode().equals("US") && c.getName().equals("United States")
        ));
    }

    @DisplayName("입력 리스트가 비어 있으면 아무 일도 하지 않는다")
    @Test
    void upsertCountries_doNothingWhenEmpty() {
        // when
        countrySyncService.upsertCountries(List.of());

        // then
        verifyNoInteractions(countryRepository);
        verifyNoInteractions(holidayDomainMapper);
    }

}
