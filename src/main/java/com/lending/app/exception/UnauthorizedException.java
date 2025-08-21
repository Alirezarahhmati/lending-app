package com.lending.app.exception;

import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
        super(ResponseCode.UNAUTHORIZED);
    }
}
