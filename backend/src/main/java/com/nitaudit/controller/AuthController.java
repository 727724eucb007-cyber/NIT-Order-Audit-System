package com.nitaudit.controller;

import com.nitaudit.dto.LoginRequest;
import com.nitaudit.dto.LoginResponse;
import com.nitaudit.model.User;
import com.nitaudit.repository.UserRepository;
import com.nitaudit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userRepo.findByUsername(req.getUsername())
            .filter(u -> u.isActive() && encoder.matches(req.getPassword(), u.getPassword()))
            .map(u -> ResponseEntity.ok(new LoginResponse(
                jwtUtils.generateToken(u.getUsername(), u.getRole().name()),
                u.getUsername(), u.getRole().name(), u.getFullName(), u.getId()
            )))
            .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@org.springframework.security.core.annotation.AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(new LoginResponse(null, user.getUsername(),
                user.getRole().name(), user.getFullName(), user.getId()));
    }
}
