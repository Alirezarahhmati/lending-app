package com.lending.app.application.service;

import com.lending.app.model.entity.User;
import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.model.record.user.UserMessageSet;

public interface UserService {
    UserMessage save(CreateUserCommand command);
    User save(User user);
    UserMessage get();
    UserMessageSet getAll();
    UserMessage update(UpdateUserCommand command);
    User getUserForUpdate(String id);
    void delete();
    UserMessage changeScore(User user, int delta);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}


