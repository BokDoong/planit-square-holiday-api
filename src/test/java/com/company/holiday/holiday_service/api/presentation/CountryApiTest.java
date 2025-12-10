package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CountryApiTest extends ApiTestSupport {

    @DisplayName("모든 나라 목록을 조회한다.")
    @Test
    void syncCountriesAndHolidays() throws Exception {
        // when // then
        mockMvc.perform(
                        get("/api/v1/countries")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}
