package com.company.holiday.holiday_service.clients.nager;

import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
public class NagerRestClient implements NagerClient {

    private static final String AVAILABLE_COUNTRIES_PATH = "/AvailableCountries";
    private static final String PUBLIC_HOLIDAYS_PATH = "/PublicHolidays/{year}/{countryCode}";
    private static final int RETRY_MAX = 3;

    private final RestClient nagerRestClient;

    public NagerRestClient(@Qualifier("nagerHttpClient") RestClient nagerRestClient) {
        this.nagerRestClient = nagerRestClient;
    }

    @Override
    public List<NagerAvailableCountryResponse> getAvailableCountries() {
        return callWithRetry(() -> {
            NagerAvailableCountryResponse[] body = nagerRestClient.get()
                    .uri(AVAILABLE_COUNTRIES_PATH)
                    .retrieve()
                    .body(NagerAvailableCountryResponse[].class);

            return body == null ? List.of() : Arrays.asList(body);
        }, "getAvailableCountries");
    }

    @Override
    public List<NagerPublicHolidayResponse> getPublicHolidays(int year, String countryCode) {
        return callWithRetry(() -> {
            NagerPublicHolidayResponse[] body = nagerRestClient.get()
                    .uri(PUBLIC_HOLIDAYS_PATH, year, countryCode)
                    .retrieve()
                    .body(NagerPublicHolidayResponse[].class);

            return body == null ? List.of() : Arrays.asList(body);
        }, "getPublicHolidays");
    }


    private <T> T callWithRetry(Supplier<T> action, String actionName) {

        for (int attempt = 1; attempt <= RETRY_MAX; attempt++) {

            try {
                return action.get();
            } catch (Exception e) {

                log.warn("Nager API 호출 실패 ({}), 시도 {}/{}: {}", actionName, attempt, RETRY_MAX, e.getMessage());

                if (attempt == RETRY_MAX) {
                    log.error("Nager API 호출 최종 실패 ({}), 재시도 {}회 모두 실패", actionName, RETRY_MAX);
                    throw e;
                }

            }

        }

        throw new IllegalStateException("unreachable code, 재시도 로직에서 여기까진 내려올 수 없는 구조");
    }

}
