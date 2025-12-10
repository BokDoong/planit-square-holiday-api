package com.company.holiday.holiday_service.api.presentation.dto.request;


import com.company.holiday.holiday_service.api.domain.HolidayType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record HolidaySearchRequest(

        @NotBlank
        String countryCode,

        Integer year,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        HolidayType type

) {

    @AssertTrue(message = "from 은 to 보다 이후일 수 없습니다.")
    public boolean isValidRange() {
        if (from == null || to == null) {
            return true;
        }
        return !from.isAfter(to);
    }

}
