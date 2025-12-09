package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.api.application.HolidayCommandService;
import com.company.holiday.holiday_service.api.presentation.dto.HolidayRefreshRequest;
import com.company.holiday.holiday_service.api.presentation.dto.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.HolidaySyncResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayApi {

    private final HolidayCommandService holidayCommandService;

    @PostMapping("/sync")
    public HolidaySyncResponse syncCountriesAndHolidays() {
        return holidayCommandService.syncCountriesAndHolidays();
    }

    @PostMapping("/refresh")
    public HolidayRefreshResponse refreshHolidays(@RequestBody @Valid HolidayRefreshRequest request) {
        return holidayCommandService.refreshHolidays(request.getYear(), request.getCountryCode());
    }

}
