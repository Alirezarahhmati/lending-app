package com.lending.app.application.service.impl;

import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.user.UserMessageSet;
import com.lending.app.repository.UserRepository;
import com.lending.app.application.service.UserService;
import com.lending.app.mapper.UserMapper;
import com.lending.app.util.SecurityUtils;
import com.lending.app.exception.NotFoundException;
import com.lending.app.exception.AlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

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
    @Transactional
    @CachePut(value = "users", key = "#result.id")
    @CacheEvict(value = "users_all", allEntries = true)
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

        User saved = saveInternal(userMapper.toEntity(command));
        log.info("User created successfully with id: {}", saved.getId());
        return userMapper.toMessage(saved);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#result.id")
    @CacheEvict(value = "users_all", allEntries = true)
    public User save(User user) {
        log.debug("Saving user entity with username: {}", user.getUsername());
        User saved = userRepository.save(user);
        log.info("User entity saved with id: {}", saved.getId());
        return saved;
    }

    @Override
    @Cacheable(value = "users", key = "T(com.lending.app.util.SecurityUtils).getCurrentUserId()")
    public UserMessage get() {
        String id = SecurityUtils.getCurrentUserId();
        log.debug("Fetching current user with id: {}", id);
        return userMapper.toMessage(loadUser(id));
    }

    @Override
    @Cacheable(value = "users_all", key = "#root.methodName")
    public UserMessageSet getAll() {
        log.debug("Fetching all users");
        Set<UserMessage> users = userRepository.findAll().stream()
                .map(userMapper::toMessage)
                .collect(Collectors.toSet());
        log.info("Fetched {} users", users.size());
        return new UserMessageSet(users);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#result.id")
    @CacheEvict(value = "users_all", allEntries = true)
    public UserMessage update(UpdateUserCommand command) {
        String id = SecurityUtils.getCurrentUserId();
        log.debug("Updating user with id: {}", id);
        User existing = loadUser(id);

        if (command.username() != null && !existing.getUsername().equals(command.username()) && userRepository.existsByUsername(command.username())) {
            log.warn("User update failed: username {} already exists", command.username());
            throw new AlreadyExistsException("Username");
        }
        if (command.email() != null && !existing.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email())) {
            log.warn("User update failed: email {} already exists", command.email());
            throw new AlreadyExistsException("Email");
        }

        userMapper.apply(command, existing);
        User saved = saveInternal(existing);
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
    @Transactional
    @CacheEvict(value = {"users", "users_all"}, allEntries = true)
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
    @Transactional
    @CachePut(value = "users", key = "#user.id")
    @CacheEvict(value = "users_all", allEntries = true)
    public void changeScore(User user, int delta) {
        log.debug("Changing score for userId: {} by delta: {}", user.getId(), delta);

        User existing = loadUser(user.getId());
        existing.setScore(existing.getScore() + delta);
        saveInternal(existing);

        log.info("User score updated for userId: {}. New score: {}", existing.getId(), existing.getScore());
    }

    private User saveInternal(User user) {
        log.debug("Persisting user entity with username: {}", user.getUsername());
        User saved = userRepository.save(user);
        log.info("User entity persisted with id: {}", saved.getId());
        return saved;
    }

    private User loadUser(String id) {
        log.debug("Loading user entity from database with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new NotFoundException("User");
                });
        log.info("User loaded from database with id: {}", user.getId());
        return user;
    }
}