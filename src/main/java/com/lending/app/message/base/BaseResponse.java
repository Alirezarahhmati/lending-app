package com.lending.app.message.base;


import com.lending.app.exception.base.ResponseCode;
import de.huxhorn.sulky.ulid.ULID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

public record BaseResponse<T>(
        Integer code,
        String message,
        String referenceId,
        String timestamp,
        T result
) {

    public BaseResponse(Integer code, String message, T result) {
        this(code, message, new ULID().nextULID(), Instant.now().toString(), result);
    }

    public static <T> ResponseEntity<BaseResponse<T>> success(T result) {
        return ResponseEntity.ok(new BaseResponse<>(
                ResponseCode.SUCCESS.getCode(),
                ResponseCode.SUCCESS.getMessage(),
                result
        ));
    }

    public static ResponseEntity<BaseResponse<?>> error(HttpStatus status, Integer code, String message) {
        return ResponseEntity.status(status).body(
                new BaseResponse<>(
                        code,
                        message,
                        null
                )
        );
    }
}
