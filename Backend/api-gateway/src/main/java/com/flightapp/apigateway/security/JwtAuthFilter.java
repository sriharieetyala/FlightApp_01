package com.flightapp.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil; // I injected JwtUtil to validate and extract info from JWT tokens

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        log.info(" Incoming: {} {}", method, path);

        // I allow public endpoints without authentication
        if (isPublic(path, method)) {
            log.info(" Public endpoint allowed");
            return chain.filter(exchange);
        }

        // I check for Authorization header on secured endpoints
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" Missing or invalid Authorization header");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // I extracted the JWT token from the header

        Claims claims;
        try {
            jwtUtil.validateToken(token); // I validate token expiry and signature
            claims = jwtUtil.extractAllClaims(token); // I extract claims from token
        } catch (Exception e) {
            log.warn(" JWT invalid: {}", e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String username = claims.getSubject(); // I extracted username from token
        String role = jwtUtil.extractRole(token); // I extracted role from token
        log.info(" Authenticated: {} [{}]", username, role);

        // I check if endpoint is ADMIN-only and validate user role
        if (isAdminOnly(path, method)) {
            if (!"ADMIN".equals(role)) {
                log.warn(" Forbidden - ADMIN only");
                return onError(exchange, HttpStatus.FORBIDDEN);
            }
        }

        // I pass username and role to downstream services via headers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Name", username)
                .header("X-User-Role", role != null ? role : "")
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublic(String path, HttpMethod method) {
        // I mark auth endpoints and flight search endpoints as public
        if (path.startsWith("/auth/")) {
            return true;
        }
        if (method == HttpMethod.GET && path.startsWith("/flight-service/flights")) {
            return true;
        }
        if (method == HttpMethod.POST && path.startsWith("/flight-service/flights/search")) {
            return true;
        }
        return false;
    }

    private boolean isAdminOnly(String path, HttpMethod method) {
        // I consider POST to /flights as ADMIN-only
        return (path.equals("/flight-service/flights")
                && method == HttpMethod.POST);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        // I set the response status for unauthorized or forbidden access
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // I set filter order early to authenticate requests before routing
    }
}
