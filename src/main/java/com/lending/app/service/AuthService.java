package com.lending.app.service;

import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.model.record.auth.AuthResponse;

public interface AuthService {
    AuthResponse signUp(SignUpCommand command);
    AuthResponse signIn(SignInCommand command);
}
