package com.company.holiday.holiday_service.clients.nager;

import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class NagerRestClient implements NagerClient {

    private static final String AVAILABLE_COUNTRIES_PATH = "/AvailableCountries";
    private static final String PUBLIC_HOLIDAYS_PATH = "/PublicHolidays/{year}/{countryCode}";

    private final RestClient nagerRestClient;

    public NagerRestClient(@Qualifier("nagerHttpClient") RestClient nagerRestClient) {
        this.nagerRestClient = nagerRestClient;
    }

    @Override
    public List<NagerAvailableCountryResponse> getAvailableCountries() {
        try {
            NagerAvailableCountryResponse[] body = nagerRestClient.get()
                    .uri(AVAILABLE_COUNTRIES_PATH)
                    .retrieve()
                    .body(NagerAvailableCountryResponse[].class);

            return body == null ? List.of() : Arrays.asList(body);
        } catch (RestClientException e) {
            log.warn("Nager API 가용 국가 조회 실패: {}", e.getMessage(), e);
            throw new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR, "Nager API 가용 국가 조회 중 에러 발생", e);
        }
    }

    @Override
    public List<NagerPublicHolidayResponse> getPublicHolidays(int year, String countryCode) {
        try {
            NagerPublicHolidayResponse[] body = nagerRestClient.get()
                    .uri(PUBLIC_HOLIDAYS_PATH, year, countryCode)
                    .retrieve()
                    .body(NagerPublicHolidayResponse[].class);

            return body == null ? List.of() : Arrays.asList(body);
        } catch (RestClientException e) {
            log.warn("Nager API 공휴일 조회 실패. countryCode={}, year={}, reason={}", countryCode, year, e.getMessage(), e);
            throw new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR, "Nager API 공휴일 조회 중 에러 발생", e);
        }
    }

}
