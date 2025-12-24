package com.flightapp.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email; // unique email for each user

    @Column(nullable = false)
    private String password; // will store encoded password

    @Column(nullable = false)
    private String role; // "USER" or "ADMIN"

    // Tracks when password was last changed for expiry policy
    // NULL means password has never been changed (treat as expired)
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
}
