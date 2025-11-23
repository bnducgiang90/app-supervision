package com.klkt.supervision.controller;

import com.klkt.supervision.dto.*;
import com.klkt.supervision.entity.User;
import com.klkt.supervision.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }
    
    @GetMapping("/{id}")
    public Mono<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    @GetMapping("/username/{username}")
    public Mono<UserResponse> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }
    
    @GetMapping
    public Flux<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @PutMapping("/{id}/status")
    public Mono<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
        return userService.updateUserStatus(id, userStatus);
    }
    
    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
    
    @PostMapping("/logout/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> logout(@PathVariable Long userId) {
        return userService.logout(userId);
    }
    
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }
}
