package com.lending.app.service;

import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.UserMapper;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.repository.UserRepository;
import com.lending.app.application.service.impl.UserServiceImpl;
import com.lending.app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    private User entity;
    private UserMessage message;

    @BeforeEach
    void setUp() {
        entity = new User();
        entity.setUsername("ali");
        entity.setEmail("ali@example.com");
        entity.setScore(10);

        message = new UserMessage("01HUID", "ali", "ali@example.com", 10);
    }

    @Nested
    @DisplayName("Create")
    class CreateTests {
        @Test
        void shouldCreateUserSuccessfully() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10);
            when(userRepository.existsByUsername("ali")).thenReturn(false);
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);
            when(userMapper.toEntity(any(CreateUserCommand.class))).thenReturn(entity);
            when(userRepository.save(any(User.class))).thenReturn(entity);
            when(userMapper.toMessage(entity)).thenReturn(message);

            UserMessage res = userService.save(command);

            assertThat(res).isEqualTo(message);
        }

        @Test
        void shouldThrowWhenUsernameExists() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10);
            when(userRepository.existsByUsername("ali")).thenReturn(true);

            assertThatThrownBy(() -> userService.save(command)).isInstanceOf(AlreadyExistsException.class);
        }

        @Test
        void shouldThrowWhenEmailExists() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10);
            when(userRepository.existsByUsername("ali")).thenReturn(false);
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.save(command)).isInstanceOf(AlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Get")
    class GetTests {
        @Test
        void shouldGetCurrentUser() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01HUID");
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(entity));
                when(userMapper.toMessage(entity)).thenReturn(message);

                UserMessage res = userService.get();
                assertThat(res).isEqualTo(message);
            }
        }

        @Test
        void shouldThrowWhenCurrentUserNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01MISSING");
                when(userRepository.findById("01MISSING")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.get()).isInstanceOf(NotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {
        @Test
        void shouldUpdateUserSuccessfully() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01HUID");
                UpdateUserCommand command = new UpdateUserCommand("ali", null, null, 15);
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(entity));
                doAnswer(inv -> null).when(userMapper).apply(any(UpdateUserCommand.class), any(User.class));
                when(userRepository.save(any(User.class))).thenReturn(entity);
                when(userMapper.toMessage(entity)).thenReturn(new UserMessage("01HUID", "ali", "ali@example.com", 15));

                UserMessage res = userService.update(command);
                assertThat(res.score()).isEqualTo(15);
            }
        }

        @Test
        void shouldThrowWhenUpdateUserNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01MISSING");
                UpdateUserCommand command = new UpdateUserCommand("ali", null, null, 15);
                when(userRepository.findById("01MISSING")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.update(command)).isInstanceOf(NotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("Delete")
    class DeleteTests {
        @Test
        void shouldDeleteUserSuccessfully() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01HUID");
                when(userRepository.existsById("01HUID")).thenReturn(true);

                userService.delete();

                verify(userRepository).softDeleteById("01HUID");
            }
        }

        @Test
        void shouldThrowWhenDeleteUserNotFound() {
            try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn("01MISSING");
                when(userRepository.existsById("01MISSING")).thenReturn(false);

                assertThatThrownBy(() -> userService.delete()).isInstanceOf(NotFoundException.class);
            }
        }
    }
}


