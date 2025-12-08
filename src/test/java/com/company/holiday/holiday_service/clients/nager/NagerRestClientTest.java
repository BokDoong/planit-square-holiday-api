package com.company.holiday.holiday_service.clients.nager;

import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NagerRestClientTest {

    @Mock
    RestClient restClient;

    @Mock
    RestClient.RequestHeadersUriSpec uriSpec;

    @Mock
    RestClient.RequestHeadersSpec headersSpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    NagerRestClient nagerRestClient;

    @BeforeEach
    void setUp() {
        nagerRestClient = new NagerRestClient(restClient);
    }

    @DisplayName("가용 국가 조회 호출이 3번 실패하면 예외를 던진다")
    @Test
    void getAvailableCountries_retryThreeTimesThenThrow() {
        // given
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/AvailableCountries")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.body(NagerAvailableCountryResponse[].class))
                .thenThrow(new RuntimeException("boom1"))
                .thenThrow(new RuntimeException("boom2"))
                .thenThrow(new RuntimeException("boom3"));

        // when & then
        assertThatThrownBy(() -> nagerRestClient.getAvailableCountries())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom3");

        // body()가 총 3번 호출되었는지 → 재시도 검증
        verify(responseSpec, times(3))
                .body(NagerAvailableCountryResponse[].class);
    }

}
