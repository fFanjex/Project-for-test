package org.example.projectfortest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projectfortest.dto.LoginRequest;
import org.example.projectfortest.dto.RegisterRequest;
import org.example.projectfortest.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(tokens);
    }
}
