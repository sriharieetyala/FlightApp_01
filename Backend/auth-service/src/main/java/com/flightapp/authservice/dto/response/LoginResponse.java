package com.flightapp.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String role;
    private boolean passwordExpired; // true if password is older than 30 days
}
