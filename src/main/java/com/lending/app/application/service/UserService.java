package com.lending.app.application.service;

import com.lending.app.model.entity.User;
import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;

import java.util.List;

public interface UserService {
    UserMessage save(CreateUserCommand command);
    UserMessage get();
    User getUser(String id);
    List<UserMessage> getAll();
    UserMessage update(UpdateUserCommand command);
    UserMessage decreaseScore(String userId, Integer score);
    UserMessage increaseScore(String userId, Integer score);
    void delete();
}


