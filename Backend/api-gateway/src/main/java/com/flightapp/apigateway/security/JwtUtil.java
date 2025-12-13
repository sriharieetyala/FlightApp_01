package com.flightapp.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key; // I store the secret key used for signing and validating JWTs

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // I convert the secret string into a Key object for HMAC SHA signing
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        // I parse the JWT and extract all claims; this will throw if token is invalid
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        // I get the username (subject) from the token
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        // I get the role claim from the token, if present
        Object role = extractAllClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public boolean isTokenExpired(String token) {
        // I check if the token expiration date is before current time
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    public void validateToken(String token) {
        // I validate the token expiration; any other invalid token issues will throw in extractAllClaims
        if (isTokenExpired(token)) {
            throw new RuntimeException("Token expired");
        }
    }
}
