package com.klkt.supervision.controller;

import com.klkt.supervision.service.SSEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {
    
    private final SSEService sseService;
    
    /**
     * SSE endpoint for real-time updates
     * Client connects to this endpoint to receive real-time messages
     * 
     * Usage: 
     * const eventSource = new EventSource('http://localhost:8080/api/sse/stream?userId=1');
     * eventSource.addEventListener('new_message', (event) => {
     *     const message = JSON.parse(event.data);
     *     console.log('New message:', message);
     * });
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEvents(@RequestParam Long userId) {
        log.info("SSE connection established for user {}", userId);
        return sseService.subscribe(userId);
    }
    
    /**
     * Get connection status
     */
    @GetMapping("/status")
    public Mono<Map<String, Object>> getStatus() {
        return Mono.just(Map.of(
                "connectedUsers", sseService.getConnectedUserCount(),
                "status", "running"
        ));
    }
    
    /**
     * Check if a specific user is connected
     */
    @GetMapping("/status/{userId}")
    public Mono<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        return Mono.just(Map.of(
                "userId", userId,
                "connected", sseService.isUserConnected(userId)
        ));
    }
    
    /**
     * Manually disconnect a user (for testing/admin purposes)
     */
    @DeleteMapping("/disconnect/{userId}")
    public Mono<Void> disconnectUser(@PathVariable Long userId) {
        sseService.disconnectUser(userId);
        return Mono.empty();
    }
}
