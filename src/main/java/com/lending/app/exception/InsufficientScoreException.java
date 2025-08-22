package com.lending.app.exception;

import com.lending.app.exception.base.BaseException;
import com.lending.app.exception.base.ResponseCode;

public class InsufficientScoreException extends BaseException {
    public InsufficientScoreException() {
        super(ResponseCode.INSUFFICIENT_SCORE_EXCEPTION);
    }
}
