package com.lending.app.exception;

import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;

public class NotFoundException extends BaseException {
    public NotFoundException(String field) {
        super(ResponseCode.NOT_FOUND_EXCEPTION, field);
    }
}


