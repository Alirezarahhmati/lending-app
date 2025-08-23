package com.lending.app.service;

import com.lending.app.application.service.impl.JpaUserDetailsService;
import com.lending.app.model.entity.User;
import com.lending.app.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService service;

    private static final String USERNAME = "alireza";
    private static final String PASSWORD = "password";

    private User withUser() {
        User entity = new User();
        entity.setUsername(JpaUserDetailsServiceTest.USERNAME);
        entity.setPassword(JpaUserDetailsServiceTest.PASSWORD);
        return entity;
    }

    @Nested
    class LoadUserByUsernameTests {

        @Test
        void shouldReturnUserDetailsWhenUserExists() {
            User user = withUser();

            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            UserDetails result = service.loadUserByUsername(USERNAME);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getPassword()).isEqualTo(PASSWORD);
            assertThat(result.getAuthorities()).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername(USERNAME))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("User not found: " + USERNAME);
        }
    }
}