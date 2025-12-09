package com.company.holiday.holiday_service.api.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @NoArgsConstructor
public class HolidayRefreshRequest {

    @NotBlank
    private String countryCode;

    @NotNull
    private Integer year;

}
