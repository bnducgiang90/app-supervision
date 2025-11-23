package com.klkt.supervision.config;

import com.klkt.supervision.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    
    private final JwtService jwtService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip JWT processing for public endpoints - let them through without authentication
        if (isPublicEndpoint(path)) {
            log.debug("Skipping JWT authentication for public endpoint: {}", path);
            return chain.filter(exchange);
        }
        
        // For protected endpoints, extract and validate JWT token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        // Check if Authorization header is present and has Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for protected path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
            return exchange.getResponse().setComplete();
        }
        
        String token = authHeader.substring(7).trim();
        
        // Validate token
        if (token.isEmpty() || !jwtService.validateToken(token)) {
            log.warn("Invalid or expired JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
            return exchange.getResponse().setComplete();
        }
        
        try {
            // Extract user information from token
            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            
            // Validate extracted claims
            if (username == null || username.isEmpty() || userId == null || role == null || role.isEmpty()) {
                log.warn("Invalid token claims (missing username, userId, or role) for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
                return exchange.getResponse().setComplete();
            }
            
            // Create authorities from role
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
            );
            
            // Create authentication object
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );
            
            // Store user information in exchange attributes for easy access in controllers
            exchange.getAttributes().put("userId", userId);
            exchange.getAttributes().put("username", username);
            exchange.getAttributes().put("role", role);
            
            log.debug("JWT authentication successful for user: {} (id: {}, role: {}) on path: {}", 
                    username, userId, role, path);
            
            // Continue filter chain with authentication set in SecurityContext
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    
        } catch (Exception e) {
            log.error("Error processing JWT token for path {}: {}", path, e.getMessage(), e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("WWW-Authenticate", "Bearer");
            return exchange.getResponse().setComplete();
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        // Public endpoints that don't require authentication
        // These must match the permitAll() paths in SecurityConfig
        return path.equals("/api/users/login") ||
               path.equals("/api/users/register") ||
               path.startsWith("/api/sse") ||
               path.startsWith("/api/files") || // File serving - browser can't send auth headers for img/video tags
               path.startsWith("/v3/api-docs") || // Swagger/OpenAPI
               path.equals("/swagger-ui.html") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/static") ||
               path.equals("/") ||
               path.equals("/index.html") ||
               path.equals("/favicon.ico"); // Browser automatically requests favicon
    }
}

