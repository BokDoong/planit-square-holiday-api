package com.company.holiday.holiday_service.global.error.exception;

import com.company.holiday.holiday_service.global.error.ErrorCode;

public class InvalidValueException extends BusinessException {

    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidValueException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
