package com.company.holiday.holiday_service.global.error.exception;

import com.company.holiday.holiday_service.global.error.ErrorCode;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
