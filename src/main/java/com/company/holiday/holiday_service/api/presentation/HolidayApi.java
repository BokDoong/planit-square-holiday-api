package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.api.application.HolidayCommandService;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayDeleteRequest;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayRefreshRequest;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySyncResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping
    public int deleteHolidays(@RequestBody @Valid HolidayDeleteRequest request) {
        return holidayCommandService.deleteHolidays(request.getYear(), request.getCountryCode());
    }

}
