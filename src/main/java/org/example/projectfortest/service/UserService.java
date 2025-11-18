package org.example.projectfortest.service;

import org.example.projectfortest.config.JwtTokenProvider;
import org.example.projectfortest.dto.RegisterRequest;
import org.example.projectfortest.entity.User;
import org.example.projectfortest.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Map<String, Object> register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(u -> { throw new RuntimeException("Email is already in use"); });

        String encoded = passwordEncoder.encode(request.getPassword());
        User user = User.builder().email(request.getEmail()).password(encoded).build();
        User saved = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("user", saved);
        result.put("accessToken", jwtTokenProvider.generateAccessToken(saved));
        result.put("refreshToken", jwtTokenProvider.generateRefreshToken(saved));
        return result;
    }

    public Map<String, String> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid email or password");

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtTokenProvider.generateAccessToken(user));
        tokens.put("refreshToken", jwtTokenProvider.generateRefreshToken(user));
        return tokens;
    }

    public Map<String, String> recoveryPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email is empty"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        Map<String, String> result = new HashMap<>();
        result.put("message", "Password updated successfully");
        result.put("email", user.getEmail());
        return result;
    }
}