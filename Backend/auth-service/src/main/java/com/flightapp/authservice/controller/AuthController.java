package com.flightapp.authservice.controller;

import com.flightapp.authservice.dto.request.*;
import com.flightapp.authservice.dto.response.*;

import com.flightapp.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService; // I injected the UserService to handle all user-related operations

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        // I am calling the signup service method to create a new user account
        SignupResponse response = userService.signup(request);
        // I am returning the CREATED status to indicate successful registration
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // I am calling the login service method to authenticate the user
        LoginResponse response = userService.login(request);
        // I am returning OK status with the login response (usually token + user info)
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // I prepared a simple response map as logout does not need a service call in stateless JWT auth
        Map<String, String> body = new HashMap<>();
        // I added the status and message to confirm logout action
        body.put("status", "OK");
        body.put("message", "Logged out successfully.");
        // I am returning OK status with the logout message
        return ResponseEntity.ok(body);
    }
}
