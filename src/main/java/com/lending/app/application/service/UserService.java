package com.lending.app.application.service;

import com.lending.app.model.entity.User;
import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;

import java.util.List;

public interface UserService {
    UserMessage save(CreateUserCommand command);
    User save(User user);
    UserMessage get();
    User getUser(String id);
    List<UserMessage> getAll();
    UserMessage update(UpdateUserCommand command);
    User getUserForUpdate(String id);
    void delete();
    void changeScore(User user, int delta);
}


