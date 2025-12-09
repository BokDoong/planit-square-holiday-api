package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HolidayApiTest extends ApiTestSupport {

    @DisplayName("나라, 공휴일 정보에 대한 동기화를 진행한다(일괄 적재).")
    @Test
    void syncCountriesAndHolidays() throws Exception {
        // when // then
        mockMvc.perform(
                        post("/api/v1/holidays/sync")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}
