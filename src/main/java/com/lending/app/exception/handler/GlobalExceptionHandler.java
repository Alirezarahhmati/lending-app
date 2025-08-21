package com.lending.app.exception.handler;

import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.exception.UnauthorizedException;
import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;
import com.lending.app.model.record.base.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    private ResponseEntity<BaseResponse<?>> build(BaseException ex, HttpStatus status) {
        ResponseCode responseCode = ex.getCode();
        return BaseResponse.error(
                status,
                responseCode.getCode(),
                responseCode.getMessage()
        );
    }
}
