package com.lending.app.service.impl;

import com.lending.app.exception.UnauthorizedException;
import com.lending.app.model.entity.User;
import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.model.record.auth.AuthResponse;
import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.repository.UserRepository;
import com.lending.app.security.JwtService;
import com.lending.app.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse signUp(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new AlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new AlreadyExistsException("Email");
        }

        User toSave = new User();
        toSave.setUsername(command.username());
        toSave.setPassword(passwordEncoder.encode(command.password()));
        toSave.setEmail(command.email());
        toSave.setScore(0);
        userRepository.save(toSave);

        String token = jwtService.generateToken(toSave.getUsername(), Map.of("uid", String.valueOf(toSave.getId())));
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse signIn(SignInCommand command) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.username(), command.password())
            );
            if (!authentication.isAuthenticated()) {
                throw new UnauthorizedException();
            }
            String token = jwtService.generateToken(command.username(), Map.of());
            return new AuthResponse(token);
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException();
        }
    }
}
