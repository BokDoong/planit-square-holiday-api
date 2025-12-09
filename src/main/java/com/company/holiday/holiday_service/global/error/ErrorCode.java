package com.company.holiday.holiday_service.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    INVALID_REQUEST("C-001", BAD_REQUEST, "잘못된 요청 데이터입니다."),
    ENTITY_NOT_FOUND("C-002", NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    EXTERNAL_API_ERROR("C-003", BAD_GATEWAY, "외부 API 요청 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR("C-004", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    YEAR_OUT_OF_RANGE("C-005", BAD_REQUEST, "허용되지 않은 연도 범위입니다. 2021~2025 내로 입력해주세요."),

    ;

    private final String code;
    private final HttpStatus status;
    private final String message;

}
