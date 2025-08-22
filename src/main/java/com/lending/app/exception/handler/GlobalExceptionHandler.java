package com.lending.app.exception.handler;

import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.exception.NotFoundException;
import com.lending.app.exception.UnauthorizedException;
import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;
import com.lending.app.model.record.base.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({AlreadyExistsException.class})
    public ResponseEntity<BaseResponse<?>> handleAlreadyExist(AlreadyExistsException ex) {
        log.error("Conflict error: {}", ex.getMessage());
        return build(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<BaseResponse<?>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return build(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<BaseResponse<?>> handleNotFound(NotFoundException ex) {
        log.error("Not found error: {}", ex.getMessage());
        return build(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        if (ex instanceof BaseException baseEx) {
            return build(baseEx, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return BaseResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ResponseCode.INTERNAL_SERVER_ERROR.getCode(),
                ResponseCode.INTERNAL_SERVER_ERROR.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(ResponseCode.VALIDATION_EXCEPTION.getMessage());
        log.error("Validation error: {}", message);
        return BaseResponse.error(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_EXCEPTION.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse(ResponseCode.VALIDATION_EXCEPTION.getMessage());
        log.error("Constraint violation: {}", message);
        return BaseResponse.error(HttpStatus.BAD_REQUEST, ResponseCode.VALIDATION_EXCEPTION.getCode(), message);
    }

    private ResponseEntity<BaseResponse<?>> build(BaseException ex, HttpStatus status) {
        ResponseCode responseCode = ex.getCode();
        return BaseResponse.error(
                status,
                responseCode.getCode(),
                ex.getMessage()
        );
    }
}
