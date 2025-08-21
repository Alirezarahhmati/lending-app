package com.lending.app.controller;

import com.lending.app.record.auth.AuthResponse;
import com.lending.app.record.auth.SignInCommand;
import com.lending.app.record.auth.SignUpCommand;
import com.lending.app.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(command));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@RequestBody SignInCommand command) {
        return ResponseEntity.ok(authService.signIn(command));
    }
}
