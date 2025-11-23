package com.klkt.supervision.service;

import com.klkt.supervision.dto.*;
import com.klkt.supervision.entity.User;
import com.klkt.supervision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }
                    
                    // Determine role - default to MEMBER if not specified
                    User.UserRole userRole = User.UserRole.MEMBER;
                    if (request.getRole() != null && !request.getRole().isEmpty()) {
                        try {
                            userRole = User.UserRole.valueOf(request.getRole().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            // Invalid role, use default MEMBER
                            userRole = User.UserRole.MEMBER;
                        }
                    }
                    
                    User user = User.builder()
                            .username(request.getUsername())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .passwordPlain(request.getPassword())
                            .displayName(request.getDisplayName())
                            .avatarUrl(request.getAvatarUrl())
                            .role(userRole)
                            .status(User.UserStatus.ONLINE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    
                    return userRepository.save(user)
                            .map(this::toUserResponse)
                            .doOnSuccess(u -> log.info("Created user: {}", u.getUsername()));
                });
    }
    
    public Mono<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
    
    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toUserResponse)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
    
    public Flux<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .map(this::toUserResponse);
    }
    
    public Mono<UserResponse> updateUserStatus(Long userId, User.UserStatus status) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setStatus(status);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .map(this::toUserResponse)
                .doOnSuccess(u -> log.info("Updated user {} status to {}", userId, status));
    }
    
    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        // Update status to ONLINE
                        user.setStatus(User.UserStatus.ONLINE);
                        user.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(user)
                                .map(savedUser -> {
                                    // Generate JWT token
                                    String token = jwtService.generateToken(
                                            savedUser.getId(),
                                            savedUser.getUsername(),
                                            savedUser.getRole() != null ? savedUser.getRole().name() : "MEMBER"
                                    );
                                    
                                    // Build LoginResponse with token
                                    return LoginResponse.builder()
                                            .token(token)
                                            .id(savedUser.getId())
                                            .username(savedUser.getUsername())
                                            .displayName(savedUser.getDisplayName())
                                            .avatarUrl(savedUser.getAvatarUrl())
                                            .role(savedUser.getRole() != null ? savedUser.getRole().name() : "MEMBER")
                                            .status(savedUser.getStatus().name())
                                            .createdAt(savedUser.getCreatedAt())
                                            .build();
                                })
                                .doOnSuccess(response -> log.info("User {} logged in successfully with JWT token", response.getUsername()));
                    } else {
                        return Mono.error(new RuntimeException("Invalid username or password"));
                    }
                });
    }
    
    public Mono<Void> logout(Long userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setStatus(User.UserStatus.OFFLINE);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .then()
                .doOnSuccess(v -> log.info("User {} logged out", userId));
    }
    
    public Mono<Void> resetPassword(ResetPasswordRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    user.setPasswordPlain(request.getNewPassword());
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Password reset for user: {}", request.getUsername()));
    }
    
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() != null ? user.getRole().name() : "MEMBER")
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
