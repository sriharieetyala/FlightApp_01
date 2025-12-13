package com.flightapp.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // We are building a stateless REST API, so disable CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Authorize requests
                .authorizeHttpRequests(auth -> auth
                        // allow our test endpoints without any authentication
                        .requestMatchers("/auth/test/**").permitAll()
                        // for now, allow everything (we'll tighten this later)
                        .anyRequest().permitAll()
                )

                // Disable login form, keep it simple for API
                .formLogin(AbstractHttpConfigurer::disable)

                // You can leave httpBasic if you want, it's not used now anyway
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
