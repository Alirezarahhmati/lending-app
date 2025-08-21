package com.lending.app.controller;

import com.lending.app.application.service.UserService;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

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
        log.debug("Received request to get user");
        UserMessage user = userService.get();
        log.debug("User received endpoint completed for ID: {}", user.id());
        return BaseResponse.success(user);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<UserMessage>>> getAll() {
        log.debug("Received request to get all users");
        List<UserMessage> users = userService.getAll();
        log.debug("User received endpoint completed for all users: {}", users);
        return BaseResponse.success(users);
    }

    @PutMapping
    public ResponseEntity<BaseResponse<UserMessage>> update(@Valid @RequestBody UpdateUserCommand command) {
        log.info("Received request to update user");
        UserMessage updated = userService.update(command);
        log.info("User update endpoint completed for ID: {}", updated.id());
        return BaseResponse.success(updated);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        log.info("Received request to delete user");
        userService.delete();
        log.info("User deletion endpoint completed");
    }
}


