package com.flightapp.apigateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
//@Component
@RequiredArgsConstructor
public class JwtLoggingFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil; // I injected JwtUtil to validate and extract info from JWT tokens for logging

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // I log the incoming request path
        log.info(" Request to: {}", path);

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // I check if Authorization header exists and starts with Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // I extract the token

            try {
                // I validate the JWT token
                jwtUtil.validateToken(token);
                // I extract claims from the token for logging
                Claims claims = jwtUtil.extractAllClaims(token);

                // I log the authenticated user's username and role
                log.info(" Authenticated user: {}, role: {}",
                        claims.getSubject(),
                        claims.get("role"));

            } catch (Exception ex) {
                // I log a warning if the token is invalid
                log.warn(" Invalid JWT: {}", ex.getMessage());
            }
        } else {
            // I log info for requests without Authorization header
            log.info(" No Auth header found (public or unauthenticated request)");
        }

        // I continue the filter chain
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // I run this filter early to log requests before routing
    }
}
