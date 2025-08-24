package com.lending.app.controller;

import com.lending.app.application.service.UserService;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.model.record.user.UserMessageSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<BaseResponse<UserMessage>> getById() {
        UserMessage user = userService.get();
        return BaseResponse.success(user);
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<UserMessageSet>> getAll() {
        UserMessageSet users = userService.getAll();
        return BaseResponse.success(users);
    }

    @PutMapping
    public ResponseEntity<BaseResponse<UserMessage>> update(@Valid @RequestBody UpdateUserCommand command) {
        UserMessage updated = userService.update(command);
        return BaseResponse.success(updated);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        userService.delete();
    }
}


