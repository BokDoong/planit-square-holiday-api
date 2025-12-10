package com.company.holiday.holiday_service;

import com.company.holiday.holiday_service.api.application.CountryQueryService;
import com.company.holiday.holiday_service.api.application.HolidayCommandService;
import com.company.holiday.holiday_service.api.presentation.CountryApi;
import com.company.holiday.holiday_service.api.presentation.HolidayApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        HolidayApi.class, CountryApi.class
})
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    protected HolidayCommandService holidayCommandService;

    @MockitoBean
    protected CountryQueryService countryQueryService;

}
