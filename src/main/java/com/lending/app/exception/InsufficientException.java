package com.lending.app.exception;

import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;

public class InsufficientException extends BaseException {
    public InsufficientException(String field) {
        super(ResponseCode.INSUFFICIENT_EXCEPTION, field);
    }
}
