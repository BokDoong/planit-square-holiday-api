package com.company.holiday.holiday_service.api.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @NoArgsConstructor
@Schema(description = "국가/연도별 공휴일 삭제 요청")
public class HolidayDeleteRequest {

    @Schema(description = "국가 코드", example = "KR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String countryCode;

    @Schema(description = "연도 (최근 5년 내)", example = "2025", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer year;

}
