package com.company.holiday.holiday_service.global.error.exception;

import com.company.holiday.holiday_service.global.error.ErrorCode;

public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, long id) {
        super(errorCode, "id " + id + " is not found");
    }

}
