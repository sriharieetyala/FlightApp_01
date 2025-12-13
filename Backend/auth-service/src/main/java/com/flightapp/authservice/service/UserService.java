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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository; // I injected the UserRepository to interact with the DB for user data
    private final PasswordEncoder passwordEncoder; // I used PasswordEncoder to securely hash the password
    private final JwtService jwtService; // I used JwtService to generate JWT tokens during login

    @Value("${admin.signup.secret}")
    private String adminSignupSecret; // I fetched the admin secret key from application properties for secure admin creation

    public SignupResponse signup(SignupRequest request) {

        // I checked if the username already exists; if yes, I throw 400 BAD_REQUEST
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username already exists"
            );
        }

        // I set the default role as USER; only if correct secret is provided, I mark the user as ADMIN
        String role = "USER";
        if (request.getAdminSecret() != null &&
                request.getAdminSecret().equals(adminSignupSecret)) {
            role = "ADMIN";
        }

        // I created a new User object and encoded the password before saving to the database
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        // I saved the new user into the database
        user = userRepository.save(user);

        // I returned a simple signup response containing user details (without password)
        return new SignupResponse(user.getId(), user.getUsername(), user.getRole());
    }

    public LoginResponse login(LoginRequest request) {

        // I fetched the user by username; if not found, I returned 400 BAD_REQUEST
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid username or password"
                        ));

        // I verified the entered password with the stored hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid username or password"
            );
        }

        // I generated a JWT token for the successfully authenticated user
        String token = jwtService.generateToken(user);

        // I returned the login response with token and user info
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}
