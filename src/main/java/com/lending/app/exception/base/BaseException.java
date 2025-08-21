package com.lending.app.exception.base;

import lombok.Getter;

@Getter
public class BaseException  extends RuntimeException {

    private final ResponseCode code;

    public BaseException(ResponseCode code, Object... args) {
        super(code.formatMessage(args));
        this.code = code;
    }
}
