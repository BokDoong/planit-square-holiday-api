package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.presentation.dto.HolidaySyncResponse;
import com.company.holiday.holiday_service.clients.nager.NagerClient;
import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayCommandServiceTest {

    @Mock
    NagerClient nagerClient;

    @Mock
    HolidayCommandMapper mapper;

    @Mock
    CountrySyncService countrySyncService;

    @Mock
    HolidaySyncService holidaySyncService;

    HolidayCommandService holidayCommandService;

    @BeforeEach
    void setUp() {
        holidayCommandService = new HolidayCommandService(
                nagerClient,
                mapper,
                countrySyncService,
                holidaySyncService
        );
    }

    @DisplayName("가용 국가 조회 → 국가 Upsert → 나라별 최근 5년 공휴일 Upsert 순으로 호출되고, 응답에 국가수/공휴일 수를 담아 리턴한다")
    @Test
    void syncCountriesAndHolidays() {
        // given
        // 1) Nager 국가 응답
        var krResponse = new NagerAvailableCountryResponse("KR", "Korea");
        var usResponse = new NagerAvailableCountryResponse("US", "United States");

        given(nagerClient.getAvailableCountries())
                .willReturn(List.of(krResponse, usResponse));

        // 2) Mapper 변환 (외부 DTO → CountryUpsertCommand)
        var krCommand = new CountryUpsertCommand("KR", "Korea");
        var usCommand = new CountryUpsertCommand("US", "United States");

        given(mapper.toCountryCommand(krResponse)).willReturn(krCommand);
        given(mapper.toCountryCommand(usResponse)).willReturn(usCommand);

        // 3) 공휴일 응답 + HolidayUpsertCommand 변환
        var krHolidayDto = mock(NagerPublicHolidayResponse.class);
        var usHolidayDto = mock(NagerPublicHolidayResponse.class);

        var krHolidayCmd = mock(HolidayUpsertCommand.class);
        var usHolidayCmd = mock(HolidayUpsertCommand.class);

        // 각 국가·연도마다 1개 공휴일이 있다고 가정
        given(nagerClient.getPublicHolidays(anyInt(), eq("KR")))
                .willReturn(List.of(krHolidayDto));
        given(nagerClient.getPublicHolidays(anyInt(), eq("US")))
                .willReturn(List.of(usHolidayDto));

        given(mapper.toHolidayCommand(krHolidayDto)).willReturn(krHolidayCmd);
        given(mapper.toHolidayCommand(usHolidayDto)).willReturn(usHolidayCmd);

        // when
        HolidaySyncResponse response = holidayCommandService.syncCountriesAndHolidays();

        // then
        // 1) 국가 upsert 가 한 번 호출되고, 변환된 커맨드 리스트가 그대로 전달된다.
        verify(countrySyncService).upsertCountries(List.of(krCommand, usCommand));

        // 2) 나라별로 최근 5년 공휴일을 한 번에 upsert한다. 국가 2개 → 2번 호출
        verify(holidaySyncService, times(2))
                .upsertRecentFiveYearsHolidays(anyString(), anyList());
        verify(holidaySyncService).upsertRecentFiveYearsHolidays(eq("KR"), anyList());
        verify(holidaySyncService).upsertRecentFiveYearsHolidays(eq("US"), anyList());

        // 3) 응답 값 검증 (국가 수 = 2, 공휴일 수 = 2개 국가 × 5년 × 연도당 1개)
        assertThat(response.countriesCount()).isEqualTo(2);
        assertThat(response.holidaysCount()).isEqualTo(2 * 5 * 1);
    }

    @DisplayName("가용 국가 조회에서 ExternalApiException이 발생하면 그대로 전파하고 이후 동기화는 수행하지 않는다")
    @Test
    void syncCountriesAndHolidays_throwExternalApiException_CountriesFetch() {
        // given
        given(nagerClient.getAvailableCountries())
                .willThrow(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR));

        // when & then
        assertThatThrownBy(() -> holidayCommandService.syncCountriesAndHolidays())
                .isInstanceOf(ExternalApiException.class);

        verifyNoInteractions(countrySyncService);
        verifyNoInteractions(holidaySyncService);
        verifyNoInteractions(mapper);
    }

}
