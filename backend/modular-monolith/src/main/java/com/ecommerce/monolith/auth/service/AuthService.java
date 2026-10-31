
package com.ecommerce.monolith.auth.service;

import com.ecommerce.monolith.auth.dto.AuthResponse;
import com.ecommerce.monolith.auth.dto.LoginRequest;
import com.ecommerce.monolith.auth.dto.RegisterRequest;
import com.ecommerce.monolith.security.service.JwtService;
import com.ecommerce.monolith.user.entity.User;
import com.ecommerce.monolith.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;



import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // User Registration
    public AuthResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setEmail(email);

        // Never store plain-text passwords
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Public registration always creates CUSTOMER
        user.setRole("CUSTOMER");

        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "Registration successful"
        );
    }

        // User Login
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                "Login successful",
                token
        );
    }
}
