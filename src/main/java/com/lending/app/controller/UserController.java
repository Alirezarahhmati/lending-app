package com.lending.app.controller;

import com.lending.app.record.user.CreateUserCommand;
import com.lending.app.record.user.UpdateUserCommand;
import com.lending.app.record.user.UserMessage;
import com.lending.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserMessage> getById(@PathVariable UUID id) {
        log.debug("Received request to get user by ID: {}", id);
        UserMessage user = userService.getById(id);
        log.debug("User received endpoint completed for ID: {}", user.id());
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserMessage>> getAll() {
        log.debug("Received request to get all users");
        List<UserMessage> users = userService.getAll();
        log.debug("User received endpoint completed for all users: {}", users);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserMessage> update(@PathVariable UUID id, @RequestBody UpdateUserCommand command) {
        log.info("Received request to update user with ID: {}", id);
        UserMessage updated = userService.update(id, command);
        log.info("User update endpoint completed for ID: {}", updated.id());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        log.info("Received request to delete user with ID: {}", id);
        userService.delete(id);
        log.info("User deletion endpoint completed for ID: {}", id);
    }
}


