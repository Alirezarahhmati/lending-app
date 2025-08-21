package com.lending.app.service;

import com.lending.app.record.auth.SignInCommand;
import com.lending.app.record.auth.SignUpCommand;
import com.lending.app.record.auth.AuthResponse;

public interface AuthService {
    AuthResponse signUp(SignUpCommand command);
    AuthResponse signIn(SignInCommand command);
}
