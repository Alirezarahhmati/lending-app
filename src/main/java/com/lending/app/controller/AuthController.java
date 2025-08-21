package com.lending.app.controller;

import com.lending.app.model.record.auth.AuthMessage;
import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<AuthMessage>> signUp(@Valid @RequestBody SignUpCommand command) {
        return BaseResponse.success(authService.signUp(command));
    }

    @PostMapping("/signin")
    public ResponseEntity<BaseResponse<AuthMessage>> signIn(@Valid @RequestBody SignInCommand command) {
        return BaseResponse.success(authService.signIn(command));
    }
}
