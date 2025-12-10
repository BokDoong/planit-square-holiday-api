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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "Holidays", description = "공휴일 동기화 및 조회 API")
public class HolidayApi {

    private final HolidayCommandService holidayCommandService;
    private final HolidayQueryService holidayQueryService;
    private final HolidayQueryMapper queryMapper;

    @PostMapping("/sync")
    @Operation(
            summary = "전체 공휴일 동기화",
            description = "최근 5년(예: 2021~2025)의 공휴일을 외부 Nager API에서 수집하여 국가/공휴일 데이터를 재적재합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "동기화 성공",
                            content = @Content(schema = @Schema(implementation = HolidaySyncResponse.class))),
                    @ApiResponse(responseCode = "500", description = "외부 API 오류 혹은 서버 내부 오류")
            }
    )
    public HolidaySyncResponse sync() {
        return holidayCommandService.syncCountriesAndHolidays();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "특정 연도의 공휴일 재동기화",
            description = "요청한 국가/연도의 공휴일을 외부 API에서 재조회 후, 기존 데이터를 삭제하고 새 데이터로 덮어씁니다."
    )
    public HolidayRefreshResponse refresh(
            @RequestBody @Valid HolidayRefreshRequest request
    ) {
        return holidayCommandService.refreshHolidays(request.getYear(), request.getCountryCode());
    }

    @GetMapping
    @Operation(
            summary = "공휴일 목록 조회",
            description = """
                    국가 코드, 기간(연도 또는 from~to), 공휴일 타입 기준으로 공휴일 목록을 페이지 단위로 조회합니다.
                    - 날짜 지정 규칙: year / from~to / 둘 다 없으면 최근 5년 전체
                    - 타입 지정 시, 해당 타입을 포함하는 공휴일만 조회합니다.
                    """
    )
    public Page<HolidaySearchResponse> search(
            @ParameterObject @Valid HolidaySearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return holidayQueryService.search(queryMapper.toQuery(request), pageable);
    }

    @DeleteMapping
    @Operation(
            summary = "국가/연도별 공휴일 삭제",
            description = "요청한 국가/연도의 공휴일을 모두 삭제합니다. 삭제된 공휴일 개수를 응답합니다."
    )
    public HolidayDeleteResponse delete(
            @RequestBody @Valid HolidayDeleteRequest request
    ) {
        return holidayCommandService.deleteHolidays(request.getYear(), request.getCountryCode());
    }
}
