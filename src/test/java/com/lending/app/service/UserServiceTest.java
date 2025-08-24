package com.lending.app.service;

import com.lending.app.exception.AlreadyExistsException;
import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.UserMapper;
import com.lending.app.model.entity.User;
import com.lending.app.model.enums.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private PasswordEncoder passwordEncoder;


    private User user;
    private UserMessage userMessage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("ali");
        user.setEmail("ali@example.com");
        user.setScore(10);

        userMessage = new UserMessage("01HUID", "ali", "ali@example.com", 10);
    }

    private void withCurrentUser(String userId, Runnable action) {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
            action.run();
        }
    }

    @Nested
    @DisplayName("Create")
    class CreateTests {
        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10, Role.USER);
            when(userRepository.existsByUsername("ali")).thenReturn(false);
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);
            when(userMapper.toEntity(any(CreateUserCommand.class))).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toMessage(user)).thenReturn(userMessage);

            UserMessage res = userService.save(command);

            assertThat(res).isEqualTo(userMessage);
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when username exists")
        void shouldThrowWhenUsernameExists() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10, Role.USER);
            when(userRepository.existsByUsername("ali")).thenReturn(true);

            assertThatThrownBy(() -> userService.save(command)).isInstanceOf(AlreadyExistsException.class);
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when email exists")
        void shouldThrowWhenEmailExists() {
            CreateUserCommand command = new CreateUserCommand("ali", "secret123", "ali@example.com", 10, Role.USER);
            when(userRepository.existsByUsername("ali")).thenReturn(false);
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.save(command)).isInstanceOf(AlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("Get")
    class GetTests {
        @Test
        @DisplayName("should get current user successfully")
        void shouldGetCurrentUser() {
            withCurrentUser("01HUID", () -> {
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(user));
                when(userMapper.toMessage(user)).thenReturn(userMessage);

                UserMessage res = userService.get();
                assertThat(res).isEqualTo(userMessage);
            });
        }

        @Test
        @DisplayName("should throw NotFoundException when current user not found")
        void shouldThrowWhenCurrentUserNotFound() {
            withCurrentUser("01MISSING", () -> {
                when(userRepository.findById("01MISSING")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.get()).isInstanceOf(NotFoundException.class);
            });
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {
        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() {
            withCurrentUser("01HUID", () -> {
                UpdateUserCommand command = new UpdateUserCommand("alireza", "adminadmin", null, 15);
                when(passwordEncoder.encode("adminadmin")).thenReturn("adminadmin");
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(user));
                doAnswer(inv -> null).when(userMapper).apply(any(UpdateUserCommand.class), any(User.class));
                when(userRepository.save(any(User.class))).thenReturn(user);
                when(userMapper.toMessage(user)).thenReturn(new UserMessage("01HUID", "ali", "ali@example.com", 15));

                UserMessage res = userService.update(command);
                assertThat(res.score()).isEqualTo(15);
            });
        }

        @Test
        @DisplayName("should throw NotFoundException when user to update not found")
        void shouldThrowWhenUpdateUserNotFound() {
            withCurrentUser("01MISSING", () -> {
                UpdateUserCommand command = new UpdateUserCommand("alireza", "adminadmin", null, 15);
                when(userRepository.findById("01MISSING")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userService.update(command)).isInstanceOf(NotFoundException.class);
            });
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when updating username to existing one")
        void shouldThrowWhenUpdateUsernameExists() {
            withCurrentUser("01HUID", () -> {
                UpdateUserCommand command = new UpdateUserCommand("existingUsername", null, null, 15);
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(user));
                when(userRepository.existsByUsername("existingUsername")).thenReturn(true);

                assertThatThrownBy(() -> userService.update(command))
                        .isInstanceOf(AlreadyExistsException.class);
            });
        }

        @Test
        @DisplayName("should throw AlreadyExistsException when updating email to existing one")
        void shouldThrowWhenUpdateEmailExists() {
            withCurrentUser("01HUID", () -> {
                UpdateUserCommand command = new UpdateUserCommand(null, null, "existing@example.com", 15);
                when(userRepository.findById("01HUID")).thenReturn(Optional.of(user));
                when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

                assertThatThrownBy(() -> userService.update(command))
                        .isInstanceOf(AlreadyExistsException.class);
            });
        }
    }

    @Nested
    @DisplayName("Delete")
    class DeleteTests {
        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            withCurrentUser("01HUID", () -> {
                when(userRepository.existsById("01HUID")).thenReturn(true);

                userService.delete();

                verify(userRepository).softDeleteById("01HUID");
            });
        }

        @Test
        @DisplayName("should throw NotFoundException when user to delete not found")
        void shouldThrowWhenDeleteUserNotFound() {
            withCurrentUser("01MISSING", () -> {
                when(userRepository.existsById("01MISSING")).thenReturn(false);

                assertThatThrownBy(() -> userService.delete()).isInstanceOf(NotFoundException.class);
            });
        }
    }

    @Nested
    @DisplayName("Change Score")
    class ChangeScoreTests {

        @Test
        @DisplayName("should change user score correctly")
        void shouldChangeScore() {
            user.setScore(10);

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            userService.changeScore(user, 5);

            assertThat(user.getScore()).isEqualTo(15);
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("Existence Checks")
    class ExistenceTests {

        @Test
        @DisplayName("should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            when(userRepository.existsByUsername("ali")).thenReturn(true);

            boolean exists = userService.existsByUsername("ali");

            assertThat(exists).isTrue();
            verify(userRepository).existsByUsername("ali");
        }

        @Test
        @DisplayName("should return false when username does not exist")
        void shouldReturnFalseWhenUsernameDoesNotExist() {
            when(userRepository.existsByUsername("ali")).thenReturn(false);

            boolean exists = userService.existsByUsername("ali");

            assertThat(exists).isFalse();
            verify(userRepository).existsByUsername("ali");
        }

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

            boolean exists = userService.existsByEmail("ali@example.com");

            assertThat(exists).isTrue();
            verify(userRepository).existsByEmail("ali@example.com");
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);

            boolean exists = userService.existsByEmail("ali@example.com");

            assertThat(exists).isFalse();
            verify(userRepository).existsByEmail("ali@example.com");
        }
    }
}