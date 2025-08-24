package com.lending.app.service;

import com.lending.app.application.service.UserService;
import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.exception.UnauthorizedException;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.auth.AuthMessage;
import com.lending.app.model.record.auth.SignInCommand;
import com.lending.app.model.record.auth.SignUpCommand;
import com.lending.app.security.JwtService;
import com.lending.app.application.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthServiceImpl authService;

    private SignUpCommand signUpCommand;
    private SignInCommand signInCommand;
    private User savedUser;

    @BeforeEach
    void setUp() {
        signUpCommand = new SignUpCommand("alireza", "password123", "alireza@example.com");
        signInCommand = new SignInCommand("alireza", "password123");

        savedUser = new User();
        savedUser.setUsername("alireza");
        savedUser.setPassword("encoded");
        savedUser.setEmail("alireza@example.com");
        savedUser.setScore(0);
    }

    private void withAuthenticatedUser(User user, Runnable action) {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        action.run();
    }

    @Nested
    @DisplayName("SignUp")
    class SignUp {

        @Test
        @DisplayName("should sign up successfully and return token with correct claims")
        void shouldSignUpSuccessfully() {
            when(userService.existsByUsername("alireza")).thenReturn(false);
            when(userService.existsByEmail("alireza@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(userService.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(eq("alireza"), any(Map.class))).thenReturn("jwt-token");

            AuthMessage res = authService.signUp(signUpCommand);

            assertThat(res).isNotNull();
            assertThat(res.token()).isEqualTo("jwt-token");

            verify(userService).save((User) argThat(user ->
                    ((User) user).getUsername().equals("alireza") &&
                            ((User) user).getEmail().equals("alireza@example.com") &&
                            ((User) user).getPassword().equals("encoded") &&
                            ((User) user).getScore() == 0
            ));
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when username exists")
        void shouldThrowOnDuplicateUsername() {
            when(userService.existsByUsername("alireza")).thenReturn(true);

            assertThatThrownBy(() -> authService.signUp(signUpCommand))
                    .isInstanceOf(AlreadyExistsException.class);

            verify(userService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when email exists")
        void shouldThrowOnDuplicateEmail() {
            when(userService.existsByUsername("alireza")).thenReturn(false);
            when(userService.existsByEmail("alireza@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.signUp(signUpCommand))
                    .isInstanceOf(AlreadyExistsException.class);

            verify(userService, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("SignIn")
    class SignIn {

        @Test
        @DisplayName("should sign in successfully and return token with correct claims")
        void shouldSignInSuccessfully() {
            withAuthenticatedUser(savedUser, () -> {
                when(jwtService.generateToken(eq("alireza"), any(Map.class))).thenReturn("jwt-token");

                AuthMessage res = authService.signIn(signInCommand);

                assertThat(res.token()).isEqualTo("jwt-token");
            });
        }

        @Test
        @DisplayName("should throw UnauthorizedException when authentication fails")
        void shouldThrowUnauthorizedOnInvalidCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> authService.signIn(signInCommand))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("should throw UnauthorizedException when authentication returns unauthenticated")
        void shouldThrowWhenUnauthenticated() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(false);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            assertThatThrownBy(() -> authService.signIn(signInCommand))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("should throw exception if principal is not User")
        void shouldThrowIfPrincipalWrongType() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("notUser");
            when(authenticationManager.authenticate(any())).thenReturn(authentication);

            assertThatThrownBy(() -> authService.signIn(signInCommand))
                    .isInstanceOf(ClassCastException.class);
        }
    }
}