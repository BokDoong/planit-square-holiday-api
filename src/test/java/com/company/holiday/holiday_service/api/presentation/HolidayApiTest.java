package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.ApiTestSupport;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayDeleteRequest;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayRefreshRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @DisplayName("특정 국가와 연도의 공휴일을 재동기화한다.")
    @Test
    void refreshHolidays() throws Exception {
        // given
        HolidayRefreshRequest request = new HolidayRefreshRequest("KR", 2024);

        // when // then
        mockMvc.perform(
                        post("/api/v1/holidays/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("countryCode가 공백이거나 year가 없으면 400을 반환한다.")
    @Test
    void refreshHolidays_validationFail() throws Exception {
        // given
        HolidayRefreshRequest invalidRequest = new HolidayRefreshRequest(" ", null);

        // when // then
        mockMvc.perform(
                        post("/api/v1/holidays/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("특정 국가와 연도의 공휴일을 삭제한다.")
    @Test
    void deleteHolidays() throws Exception {
        // given
        HolidayDeleteRequest request = new HolidayDeleteRequest("KR", 2024);

        // when // then
        mockMvc.perform(
                        delete("/api/v1/holidays")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}
