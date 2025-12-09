package com.company.holiday.holiday_service.global.error;

import com.company.holiday.holiday_service.global.error.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] code={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
        return toResponse(e.getErrorCode());
    }

    // 4xx
    @ExceptionHandler({
            BindException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestPartException.class,
            IllegalArgumentException.class
    })
    protected ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn("[BadRequest] type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
        return toResponse(ErrorCode.INVALID_REQUEST);
    }

    // 4xx - @Valid, @Validated 핸들링
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("handleMethodArgumentNotValidException", e);
        return toResponse(ErrorCode.INVALID_REQUEST, e.getBindingResult());
    }

    // 내부 5xx, 잡지 못한 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("[UnhandledException] {} {}",
                request.getMethod(), request.getRequestURI(), e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
        ErrorResponse body = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(body, errorCode.getStatus());
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, BindingResult bindingResult) {
        ErrorResponse body = ErrorResponse.of(errorCode, bindingResult);
        return new ResponseEntity<>(body, errorCode.getStatus());
    }

}
