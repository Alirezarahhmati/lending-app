package com.lending.app.application.service.impl;

import com.lending.app.application.service.UserService;
import com.lending.app.exception.UnauthorizedException;
import com.lending.app.model.entity.User;
import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.model.record.auth.AuthMessage;
import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.enums.Role;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.repository.UserRepository;
import com.lending.app.security.JwtService;
import com.lending.app.application.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${signup.bonus}")
    private int bonus;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthMessage signUp(SignUpCommand command) {
        log.info("SignUp attempt for username: {}, email: {}", command.username(), command.email());

        if (userService.existsByUsername(command.username())) {
            log.warn("SignUp failed: username {} already exists", command.username());
            throw new AlreadyExistsException(command.username());
        }
        if (userService.existsByEmail(command.email())) {
            log.warn("SignUp failed: email {} already exists", command.email());
            throw new AlreadyExistsException("Email");
        }

        CreateUserCommand toSave = new CreateUserCommand(command.username(), passwordEncoder.encode(command.password()), command.email(), bonus, Role.USER);
        UserMessage userMessage = userService.save(toSave);

        String token = jwtService.generateToken(toSave.username(), Map.of("uid", String.valueOf(userMessage.id())));
        log.info("SignUp successful for username: {}", toSave.username());
        return new AuthMessage(token);
    }

    @Override
    public AuthMessage signIn(SignInCommand command) {
        log.info("SignIn attempt for username: {}", command.username());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.username(), command.password())
            );
            if (!authentication.isAuthenticated()) {
                log.warn("SignIn failed: authentication failed for username {}", command.username());
                throw new UnauthorizedException();
            }
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user.getUsername(), Map.of("uid", user.getId()));
            log.info("SignIn successful for username: {}", user.getUsername());
            return new AuthMessage(token);
        } catch (AuthenticationException ex) {
            log.warn("SignIn failed: authentication exception for username {}", command.username());
            throw new UnauthorizedException();
        }
    }
}
