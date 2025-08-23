package com.lending.app.application.service.impl;

import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.model.entity.User;
import com.lending.app.repository.UserRepository;
import com.lending.app.application.service.UserService;
import com.lending.app.mapper.UserMapper;
import com.lending.app.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import com.lending.app.exception.NotFoundException;
import com.lending.app.exception.AlreadyExistsException;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserMessage save(CreateUserCommand command) {
        log.debug("Creating new user: {}", command.username());

        if (userRepository.existsByUsername(command.username())) {
            log.warn("User creation failed: username {} already exists", command.username());
            throw new AlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            log.warn("User creation failed: email {} already exists", command.email());
            throw new AlreadyExistsException("Email");
        }

        User saved = save(userMapper.toEntity(command));
        log.info("User created successfully with id: {}", saved.getId());
        return userMapper.toMessage(saved);
    }

    @Override
    public User save(User user) {
        log.debug("Saving user entity with username: {}", user.getUsername());
        User saved = userRepository.save(user);
        log.info("User entity saved with id: {}", saved.getId());
        return saved;
    }

    @Override
    public UserMessage get() {
        String id = SecurityUtils.getCurrentUserId();
        log.debug("Fetching current user with id: {}", id);
        return userMapper.toMessage(getUser(id));
    }

    @Override
    public User getUser(String id) {
        log.debug("Fetching user entity with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new NotFoundException("User");
                });
        log.info("User fetched with id: {}", user.getId());
        return user;
    }

    @Override
    public List<UserMessage> getAll() {
        log.debug("Fetching all users");
        List<UserMessage> users = userRepository.findAll().stream().map(userMapper::toMessage).toList();
        log.info("Fetched {} users", users.size());
        return users;
    }

    @Override
    public UserMessage update(UpdateUserCommand command) {
        String id = SecurityUtils.getCurrentUserId();
        log.debug("Updating user with id: {}", id);
        User existing = getUser(id);

        if (command.username() != null && !existing.getUsername().equals(command.username()) && userRepository.existsByUsername(command.username())) {
            log.warn("User update failed: username {} already exists", command.username());
            throw new AlreadyExistsException("Username");
        }
        if (command.email() != null && !existing.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email())) {
            log.warn("User update failed: email {} already exists", command.email());
            throw new AlreadyExistsException("Email");
        }

        userMapper.apply(command, existing);
        User saved = userRepository.save(existing);
        log.info("User updated successfully with id: {}", saved.getId());
        return userMapper.toMessage(saved);
    }

    @Override
    public User getUserForUpdate(String id) {
        log.debug("Fetching user for update with id: {}", id);
        User user = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update with id: {}", id);
                    return new NotFoundException("User");
                });
        log.info("User fetched for update with id: {}", user.getId());
        return user;
    }

    @Override
    public void delete() {
        String id = SecurityUtils.getCurrentUserId();
        log.debug("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion with id: {}", id);
            throw new NotFoundException("User");
        }
        userRepository.softDeleteById(id);
        log.info("User soft-deleted with id: {}", id);
    }

    @Override
    public void changeScore(User user, int delta) {
        log.debug("Changing score for userId: {} by delta: {}", user.getId(), delta);
        user.setScore(user.getScore() + delta);
        userRepository.save(user);
        log.info("User score updated for userId: {}. New score: {}", user.getId(), user.getScore());
    }
}