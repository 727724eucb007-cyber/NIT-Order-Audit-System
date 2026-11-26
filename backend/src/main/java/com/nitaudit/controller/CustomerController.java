package com.nitaudit.controller;

import com.nitaudit.model.Customer;
import com.nitaudit.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerRepository repo;

    @GetMapping
    public List<Customer> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALESMAN')")
    public Customer create(@RequestBody Customer c) { return repo.save(c); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALESMAN')")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer c) {
        return repo.findById(id).map(existing -> {
            existing.setName(c.getName()); existing.setGstin(c.getGstin());
            existing.setAddress(c.getAddress()); existing.setPhone(c.getPhone());
            existing.setEmail(c.getEmail());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
