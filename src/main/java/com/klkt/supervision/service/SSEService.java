package com.klkt.supervision.service;

import com.klkt.supervision.dto.SSEMessage;
import com.klkt.supervision.entity.User;
import com.klkt.supervision.repository.GroupMemberRepository;
import com.klkt.supervision.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SSEService {
    
    private final ObjectMapper objectMapper;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    
    // Map to store SSE sinks for each user
    private final Map<Long, Sinks.Many<ServerSentEvent<String>>> userSinks = new ConcurrentHashMap<>();
    
    /**
     * Subscribe user to SSE stream
     */
    public Flux<ServerSentEvent<String>> subscribe(Long userId) {
        log.info("User {} subscribing to SSE stream", userId);
        
        // Create a new sink for this user
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many()
                .multicast()
                .onBackpressureBuffer();
        
        userSinks.put(userId, sink);
        
        // Send initial connection event
        sendToUser(userId, "connected", Map.of(
                "message", "Connected to chat service",
                "userId", userId
        ));
        
        // Create heartbeat to keep connection alive
        Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(30))
                .map(seq -> ServerSentEvent.<String>builder()
                        .event("heartbeat")
                        .data("{\"timestamp\": \"" + LocalDateTime.now() + "\"}")
                        .build());
        
        // Merge heartbeat with user-specific events
        return Flux.merge(
                sink.asFlux()
                        .doOnCancel(() -> {
                            log.info("User {} unsubscribed from SSE stream", userId);
                            userSinks.remove(userId);
                        })
                        .doOnError(error -> {
                            log.error("Error in SSE stream for user {}", userId, error);
                            userSinks.remove(userId);
                        }),
                heartbeat
        );
    }
    
    /**
     * Send event to specific user
     */
    public void sendToUser(Long userId, String eventType, Object data) {
        Sinks.Many<ServerSentEvent<String>> sink = userSinks.get(userId);
        if (sink != null) {
            try {
                SSEMessage message = SSEMessage.builder()
                        .eventType(eventType)
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build();
                
                String jsonData = objectMapper.writeValueAsString(message);
                
                ServerSentEvent<String> event = ServerSentEvent.<String>builder()
                        .event(eventType)
                        .data(jsonData)
                        .build();
                
                sink.tryEmitNext(event);
                log.debug("Sent {} event to user {}", eventType, userId);
            } catch (Exception e) {
                log.error("Failed to send event to user {}", userId, e);
            }
        } else {
            log.debug("User {} not subscribed to SSE stream", userId);
        }
    }
    
    /**
     * Send event to all members of a group
     * Also includes all ADMIN users so they receive events from all groups
     */
    public void sendToGroup(Long groupId, String eventType, Object data) {
        log.info("Broadcasting {} event to group {}", eventType, groupId);
        
        // Get group members and all ADMIN users
        Mono<Set<Long>> memberIdsMono = groupMemberRepository.findByGroupId(groupId)
                .map(member -> member.getUserId())
                .collectList()
                .flatMap(memberIds -> {
                    // Also get all ADMIN users to include them in the broadcast
                    return userRepository.findAll()
                            .filter(user -> user.getRole() != null && 
                                    user.getRole() == User.UserRole.ADMIN)
                            .map(User::getId)
                            .collectList()
                            .map(adminIds -> {
                                Set<Long> allUserIds = new HashSet<>(memberIds);
                                allUserIds.addAll(adminIds);
                                return allUserIds;
                            });
                });
        
        memberIdsMono.subscribe(allUserIds -> {
            log.debug("Found {} total recipients (members + admins) for group {}", 
                    allUserIds.size(), groupId);
            allUserIds.forEach(userId -> {
                if (userSinks.containsKey(userId)) {
                    sendToUser(userId, eventType, data);
                } else {
                    log.debug("User {} is not connected, skipping SSE broadcast", userId);
                }
            });
        }, error -> {
            log.error("Error querying group members/admins for group {}", groupId, error);
            // Fallback: send to all connected users if query fails
            log.warn("Falling back to broadcast to all connected users");
            userSinks.keySet().forEach(userId -> {
                sendToUser(userId, eventType, data);
            });
        });
    }
    
    /**
     * Send event to multiple users
     */
    public void sendToUsers(Iterable<Long> userIds, String eventType, Object data) {
        userIds.forEach(userId -> sendToUser(userId, eventType, data));
    }
    
    /**
     * Check if user is connected
     */
    public boolean isUserConnected(Long userId) {
        return userSinks.containsKey(userId);
    }
    
    /**
     * Get count of connected users
     */
    public int getConnectedUserCount() {
        return userSinks.size();
    }
    
    /**
     * Disconnect user
     */
    public void disconnectUser(Long userId) {
        Sinks.Many<ServerSentEvent<String>> sink = userSinks.remove(userId);
        if (sink != null) {
            sink.tryEmitComplete();
            log.info("User {} disconnected from SSE stream", userId);
        }
    }
}
