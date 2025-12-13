package com.flightapp.authservice.controller;
import com.flightapp.authservice.entity.User;
import com.flightapp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/test")
@RequiredArgsConstructor
public class AuthTestController {

    private final UserRepository userRepository;

    @PostMapping("/create-sample-user")
    public User createSampleUser() {
        User user = User.builder()
                .username("sampleuser")
                .password("plain-temp") // we'll switch to encoded in next step
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
