package com.flightapp.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for receiving password change request from frontend.
 * 
 * WHY these fields:
 * - currentPassword: We verify the user knows their old password (security)
 * - newPassword: The new password they want to set
 * 
 * WHY @NotBlank: Ensures the field is not null or empty string
 * WHY @Size: Enforces minimum password length for security
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
