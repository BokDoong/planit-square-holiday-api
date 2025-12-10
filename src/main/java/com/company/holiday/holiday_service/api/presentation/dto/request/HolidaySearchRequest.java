package com.company.holiday.holiday_service.api.presentation.dto.request;


import com.company.holiday.holiday_service.api.domain.HolidayType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@ParameterObject
@Schema(description = "공휴일 검색 조건")
public record HolidaySearchRequest(

        @Schema(description = "조회할 국가 코드", example = "KR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String countryCode,

        @Schema(description = "조회 연도. from/to가 없을 때만 사용되며, 해당 연도 전체(1/1~12/31)를 조회", example = "2025")
        Integer year,

        @Schema(description = "조회 시작일 (yyyy-MM-dd). to와 함께 사용되며, 하나만 주어지면 나머지는 기본값 사용", example = "2024-01-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @Schema(description = "조회 종료일 (yyyy-MM-dd). from와 함께 사용되며, 하나만 주어지면 나머지는 기본값 사용", example = "2024-12-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Schema(description = "공휴일 타입 필터", example = "PUBLIC")
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
