package com.flightapp.authservice.service;

import com.flightapp.authservice.dto.response.*;
import com.flightapp.authservice.dto.request.*;

import com.flightapp.authservice.entity.User;
import com.flightapp.authservice.repository.UserRepository;
import com.flightapp.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Password expiry period in days
    private static final int PASSWORD_EXPIRY_DAYS = 30;

    @Value("${admin.signup.secret}")
    private String adminSignupSecret;

    public SignupResponse signup(SignupRequest request) {

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists");
        }

        // Set default role as USER; only if correct secret is provided, mark as ADMIN
        String role = "USER";
        if (request.getAdminSecret() != null &&
                request.getAdminSecret().equals(adminSignupSecret)) {
            role = "ADMIN";
        }

        // Create new User with passwordChangedAt set to now
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .passwordChangedAt(LocalDateTime.now()) // Set initial password change time
                .build();

        user = userRepository.save(user);

        return new SignupResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public LoginResponse login(LoginRequest request) {

        // Fetch user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid username or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid username or password");
        }

        // Check if password has expired (older than 30 days or never set)
        boolean passwordExpired = isPasswordExpired(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Return login response with password expiry status
        return new LoginResponse(token, user.getUsername(), user.getEmail(), user.getRole(), passwordExpired);
    }

    /**
     * Checks if user's password has expired.
     * Password is expired if:
     * - passwordChangedAt is NULL (legacy user, never tracked)
     * - passwordChangedAt is older than PASSWORD_EXPIRY_DAYS (30 days)
     */
    private boolean isPasswordExpired(User user) {
        if (user.getPasswordChangedAt() == null) {
            // Legacy users with no password change date are treated as expired
            return true;
        }

        long daysSinceChange = ChronoUnit.DAYS.between(user.getPasswordChangedAt(), LocalDateTime.now());
        return daysSinceChange > PASSWORD_EXPIRY_DAYS;
    }

    /**
     * Changes the password for a user and updates passwordChangedAt.
     */
    public void changePassword(String username, ChangePasswordRequest request) {

        // Find the user by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        // Verify current password matches
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect");
        }

        // Ensure new password is different from current password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from current password");
        }

        // Encode new password and update passwordChangedAt
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now()); // Reset password change timestamp
        userRepository.save(user);
    }
}
