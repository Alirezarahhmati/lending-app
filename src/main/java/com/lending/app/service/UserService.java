package com.lending.app.service;

import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserMessage create(CreateUserCommand command);
    UserMessage getById();
    List<UserMessage> getAll();
    UserMessage update(UpdateUserCommand command);
    void delete();
}


