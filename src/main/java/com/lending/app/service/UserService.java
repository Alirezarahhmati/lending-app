package com.lending.app.service;

import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserMessage create(CreateUserCommand command);
    UserMessage getById(UUID id);
    List<UserMessage> getAll();
    UserMessage update(UUID id, UpdateUserCommand command);
    void delete(UUID id);
}


