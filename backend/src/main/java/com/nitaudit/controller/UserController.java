package com.nitaudit.controller;

import com.nitaudit.model.User;
import com.nitaudit.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @GetMapping
    public List<User> getAll() { return repo.findAll(); }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody UserRequest req) {
        if (repo.existsByUsername(req.getUsername()))
            return ResponseEntity.badRequest().build();
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRole(User.Role.valueOf(req.getRole()));
        u.setFullName(req.getFullName());
        return ResponseEntity.ok(repo.save(u));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<User> toggle(@PathVariable Long id) {
        return repo.findById(id).map(u -> {
            u.setActive(!u.isActive());
            return ResponseEntity.ok(repo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Data
    static class UserRequest {
        private String username;
        private String password;
        private String role;
        private String fullName;
    }
}
