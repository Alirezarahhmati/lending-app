package com.lending.app.service.impl;

import com.lending.app.record.user.CreateUserCommand;
import com.lending.app.record.user.UpdateUserCommand;
import com.lending.app.record.user.UserMessage;
import com.lending.app.entity.User;
import com.lending.app.repository.UserRepository;
import com.lending.app.service.UserService;
import com.lending.app.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.lending.app.exception.UserNotFoundException;
import com.lending.app.exception.UsernameAlreadyExistsException;
import com.lending.app.exception.EmailAlreadyExistsException;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserMessage create(CreateUserCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }

        User saved = userRepository.save(
                userMapper.toEntity(command)
        );

        return userMapper.toMessage(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserMessage getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.forId(id));

        return userMapper.toMessage(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserMessage> getAll() {
        return userRepository.findAll().stream().map(userMapper::toMessage).toList();
    }

    @Override
    public UserMessage update(UUID id, UpdateUserCommand command) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.forId(id));
                
        if (command.username() != null && !existing.getUsername().equals(command.username()) && userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException(command.username());
        }
        if (command.email() != null && !existing.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }
        
        userMapper.apply(command, existing);
        User saved = userRepository.save(existing);

        return userMapper.toMessage(saved);
    }

    @Override
    public void delete(UUID id) {
        
        if (!userRepository.existsById(id)) {
            throw UserNotFoundException.forId(id);
        }
        
        userRepository.deleteById(id);
    }
}


