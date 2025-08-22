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
import com.lending.app.util.SecurityUtils;

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
    public ResponseEntity<BaseResponse<List<UserMessage>>> getAll() {
        List<UserMessage> users = userService.getAll();
        return BaseResponse.success(users);
    }

    @GetMapping("/version")
    public ResponseEntity<BaseResponse<Long>> getCurrentVersion() {
        String id = SecurityUtils.getCurrentUserId();
        Long version = userService.getCurrentVersion(id);
        return BaseResponse.success(version);
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


