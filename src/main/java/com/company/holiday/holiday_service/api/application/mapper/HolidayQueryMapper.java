package com.company.holiday.holiday_service.api.application.mapper;

import com.company.holiday.holiday_service.api.application.dto.HolidaySearchQuery;
import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidaySearchRequest;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySearchResponse;
import org.springframework.stereotype.Component;

@Component
public class HolidayQueryMapper {

    public HolidaySearchQuery toQuery(HolidaySearchRequest request) {
        return new HolidaySearchQuery(
                request.countryCode(),
                request.year(),
                request.from(),
                request.to(),
                request.type()
        );
    }

    public HolidaySearchResponse toResponse(Holiday holiday) {
        Country country = holiday.getCountry();

        return new HolidaySearchResponse(
                country.getCode(),
                country.getName(),
                holiday.getDate(),
                holiday.getLocalName(),
                holiday.getName(),
                holiday.isGlobal(),
                holiday.isFixed(),
                holiday.getLaunchYear(),
                holiday.getTypes(),
                holiday.getCounties()
        );
    }

}
