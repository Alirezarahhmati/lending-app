package com.lending.app.service;

import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.model.record.auth.AuthMessage;

public interface AuthService {
    AuthMessage signUp(SignUpCommand command);
    AuthMessage signIn(SignInCommand command);
}
