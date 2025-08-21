package com.lending.app.exception;

import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;

public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(String field) {
        super(ResponseCode.ALREADY_EXISTS_EXCEPTION, field);
    }
}


