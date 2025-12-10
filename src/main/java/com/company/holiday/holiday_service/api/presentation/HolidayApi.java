package com.company.holiday.holiday_service.api.presentation;

import com.company.holiday.holiday_service.api.application.HolidayCommandService;
import com.company.holiday.holiday_service.api.application.HolidayQueryService;
import com.company.holiday.holiday_service.api.application.mapper.HolidayQueryMapper;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayDeleteRequest;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidayRefreshRequest;
import com.company.holiday.holiday_service.api.presentation.dto.request.HolidaySearchRequest;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidayDeleteResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySearchResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySyncResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
public class HolidayApi {

    private final HolidayCommandService holidayCommandService;
    private final HolidayQueryService holidayQueryService;

    private final HolidayQueryMapper queryMapper;

    @PostMapping("/sync")
    public HolidaySyncResponse syncCountriesAndHolidays() {
        return holidayCommandService.syncCountriesAndHolidays();
    }

    @PostMapping("/refresh")
    public HolidayRefreshResponse refreshHolidays(@RequestBody @Valid HolidayRefreshRequest request) {
        return holidayCommandService.refreshHolidays(request.getYear(), request.getCountryCode());
    }

    @DeleteMapping
    public HolidayDeleteResponse deleteHolidays(@RequestBody @Valid HolidayDeleteRequest request) {
        return holidayCommandService.deleteHolidays(request.getYear(), request.getCountryCode());
    }

    @GetMapping
    public Page<HolidaySearchResponse> search(@Valid HolidaySearchRequest request, Pageable pageable) {
        return holidayQueryService.search(queryMapper.toQuery(request), pageable);
    }

}
