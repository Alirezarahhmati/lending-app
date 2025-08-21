package com.lending.app.service;

import com.lending.app.message.auth.SignInCommand;
import com.lending.app.message.auth.SignUpCommand;
import com.lending.app.message.auth.AuthResponse;

public interface AuthService {
    AuthResponse signUp(SignUpCommand command);
    AuthResponse signIn(SignInCommand command);
}
